[**🇺🇸 English**](README.md)

# Tài liệu Backend CSDL Thư Viện

## 1. Kiến Trúc & Thiết Kế Lược Đồ

### Tổng Quan Thực Thể
| Bảng | Mô tả | Quan hệ chính |
| --- | --- | --- |
| `TAIKHOAN` | Tài khoản nhân sự với vai trò, trạng thái và lịch sử tạo. | Khóa ngoại tự tham chiếu `CreatedBy` quản lý ủy quyền tạo tài khoản. |
| `BANDOC` | Hồ sơ bạn đọc kèm thông tin liên hệ và audit. | FK `CreatedBy → TAIKHOAN`; được `PHIEUMUON` tham chiếu. |
| `NHAXUATBAN`, `THELOAI`, `TACGIA` | Bảng danh mục cho metadata sách. | Được `SACH` tham chiếu qua khóa ngoại. |
| `SACH` | Bản ghi sách chuẩn với định dạng, giá, mô tả, tồn kho. | FK tới `TACGIA`, `THELOAI`, `NHAXUATBAN`, `TAIKHOAN`; cha của `BANSAO`. |
| `BANSAO` | Bản sao vật lý có vị trí lưu trữ và lịch sử nhập kho. | FK `ISBN → SACH`; tham gia `CT_PM` và `PHAT`. |
| `PHIEUMUON` | Phiếu mượn do thủ thư lập. | FK `IdBD → BANDOC`, `EmailNguoiLap → TAIKHOAN`; cha của `CT_PM`. |
| `CT_PM` | Dòng chi tiết từng cuốn trong phiếu mượn. | Khóa chính kép `(IdPM, MaBanSao)` + FK tới `PHIEUMUON`, `BANSAO`, `TAIKHOAN`. |
| `PHAT` | Sổ theo dõi vi phạm/phí phạt. | FK `(IdPM, MaBanSao) → CT_PM`. |
| `LICHLAM` | Lịch phân ca thủ thư với kiểm soát chồng lấn. | FK `EmailThuThu`, `CreatedBy` → `TAIKHOAN`. |
| `THONGBAO`, `THONGBAO_NGUOINHAN` | Hệ thống thông báo và người nhận. | FK cascade `Id → THONGBAO`, FK `EmailNhan → TAIKHOAN`. |
| `YEUCAU_RESETMK` | Quy trình xin cấp lại mật khẩu. | FK cho người yêu cầu và người xử lý cùng trỏ `TAIKHOAN`. |

### Mô Tả Quan Hệ (ERD)
Mô hình phản ánh nghiệp vụ thư viện: `TAIKHOAN` tạo lập dữ liệu lõi (`SACH`, `BANSAO`, `BANDOC`) và vận hành luồng `PHIEUMUON → CT_PM → PHAT`. Các bảng danh mục tách riêng thuộc tính mô tả, trong khi thông báo, lịch làm và yêu cầu reset bổ sung khía cạnh vận hành. Khóa ngoại ưu tiên `ON UPDATE CASCADE`, tránh xóa dây chuyền để bảo toàn lịch sử nghiệp vụ.

### Quyết Định Thiết Kế
- **Chuẩn hóa + audit**: Duy trì 3NF, đồng thời thu thập `CreatedAt/CreatedBy` phục vụ báo cáo (`VW_TAIKHOAN_ProfileStats`).
- **Ràng buộc nghiệp vụ**: CHECK bảo vệ enum (vai trò, trạng thái, loại phạt) và chất lượng dữ liệu (tồn kho ≥ 0, hạn trả ≥ ngày mượn).
- **Trigger chuyên trách**:
  - `TRG_SACH_Insert_Update` bắt buộc sách bản in phải khai báo số trang.
  - `TRG_CT_PM_Update` yêu cầu ghi nhận tình trạng khi đã nhập ngày trả.
  - `TRG_BANSAO_Update_SoLuongTon` đồng bộ tồn kho từ số lượng bản sao thực tế.
  - `TRG_LICHLAM_NoOverlap` đảm bảo ca làm không trùng lặp trong cùng ngày.

### View & Tính Năng Bổ Trợ
- `VW_TAIKHOAN_ProfileStats` tổng hợp KPI theo từng tài khoản.
- `UX_LICHLAM_UniqueSlot` và các UNIQUE constraint ngăn trùng lặp dữ liệu.
- Full-text catalog `LibraryCatalog` hỗ trợ tìm kiếm tiếng Việt cho tác giả và tên sách.

## 2. **Chiến Lược Tối Ưu & Lập Chỉ Mục (QUAN TRỌNG)**
Toàn bộ index được định nghĩa trong `create_indexes.sql`, kết hợp FK helper, index có điều kiện và composite phủ dữ liệu để cân bằng giữa giao dịch và báo cáo.

#### `TAIKHOAN`
| Index | Cột / Bộ lọc | Lý do |
| --- | --- | --- |
| `IX_TAIKHOAN_CreatedBy` | `(CreatedBy)` WHERE `CreatedBy IS NOT NULL` | Phục vụ view thống kê người tạo tài khoản mà không làm phình index bởi bản ghi tự tạo. |

#### `BANDOC`
| Index | Cột | Lý do |
| --- | --- | --- |
| `IX_BANDOC_CreatedBy` | `(CreatedBy)` | Tăng tốc báo cáo số bạn đọc do từng thủ thư tạo. |
| `IX_BANDOC_CreatedAt` | `(CreatedAt DESC)` | Hỗ trợ phân tích theo trục thời gian. |

#### `SACH`
| Index | Cột / INCLUDE | Lý do |
| --- | --- | --- |
| `IX_SACH_MaTacGia`, `IX_SACH_MaTheLoai`, `IX_SACH_MaNXB` | `(MaTacGia)`, `(MaTheLoai)`, `(MaNXB)` | Tối ưu JOIN với bảng danh mục và cập nhật cascade. |
| `IX_SACH_NamXuatBan` | `(NamXuatBan)` | Lọc theo năm xuất bản. |
| `IX_SACH_TenSach` | `(TenSach)` INCLUDE `(ISBN, SoLuongTon)` | Tìm kiếm tên sách và hiển thị tồn kho mà không cần lookup. |
| `IX_SACH_TheLoai_TenSach` | `(MaTheLoai, TenSach)` | Group theo thể loại và sắp xếp chữ cái. |
| `IX_SACH_CreatedBy`, `IX_SACH_CreatedAt` | `(CreatedBy)`, `(CreatedAt DESC)` | Dashboard người phụ trách và sách mới. |
| Full-text `LibraryCatalog` | `TenSach` | Tìm kiếm ngôn ngữ tự nhiên. |

#### `BANSAO`
| Index | Cột / INCLUDE / Filter | Lý do |
| --- | --- | --- |
| `IX_BANSAO_ISBN` | `(ISBN)` | JOIN về bảng mẹ `SACH`. |
| `IX_BANSAO_TinhTrang` | `(TinhTrang)` | Lọc nhanh theo trạng thái. |
| `IX_BANSAO_ISBN_TinhTrang` | `(ISBN, TinhTrang)` INCLUDE `(MaBanSao, ViTriLuuTru)` | Tìm bản sao sẵn sàng và vị trí lưu. |
| `IX_BANSAO_ViTriLuuTru` | `(ViTriLuuTru)` | Truy vấn theo ô kho. |
| `IX_BANSAO_CreatedBy`, `IX_BANSAO_CreatedAt`, `IX_BANSAO_NgayNhapKho` | `(CreatedBy)`, `(CreatedAt DESC)`, `(NgayNhapKho DESC)` | Báo cáo nhập kho theo nhân sự và thời gian. |
| `IX_BANSAO_Available` *(filtered)* | `(ISBN)` WHERE `TinhTrang = N'Có sẵn'` | Giữ truy vấn bản sao sẵn nhẹ ngay cả khi dữ liệu lịch sử lớn. |

#### `PHIEUMUON`
| Index | Cột / INCLUDE | Lý do |
| --- | --- | --- |
| `IX_PHIEUMUON_IdBD` | `(IdBD)` | Truy xuất lịch sử mượn của bạn đọc. |
| `IX_PHIEUMUON_EmailNguoiLap` | `(EmailNguoiLap)` | Theo dõi hiệu suất thủ thư. |
| `IX_PHIEUMUON_NgayMuon`, `IX_PHIEUMUON_HanTra` | `(NgayMuon DESC)`, `(HanTra)` | Tìm kiếm theo thời gian và rà soát quá hạn. |
| `IX_PHIEUMUON_IdBD_NgayMuon` | `(IdBD, NgayMuon DESC)` INCLUDE `(HanTra)` | Báo cáo chi tiết theo bạn đọc và thời điểm mượn. |

#### `CT_PM`
| Index | Cột / INCLUDE / Filter | Lý do |
| --- | --- | --- |
| `IX_CT_PM_MaBanSao` | `(MaBanSao)` | Xác định bản sao đang thuộc phiếu nào. |
| `IX_CT_PM_NgayTraThucTe` | `(NgayTraThucTe)` | Dashboard trạng thái trả sách. |
| `IX_CT_PM_EmailNguoiNhan` | `(EmailNguoiNhan)` | Theo dõi nhân sự tiếp nhận trả sách. |
| `IX_CT_PM_IdPM_NgayTraThucTe` | `(IdPM, NgayTraThucTe)` INCLUDE `(MaBanSao, TinhTrangKhiTra)` | Báo cáo từng dòng phiếu mượn. |
| `IX_CT_PM_ChuaTra` *(filtered)* | `(IdPM, MaBanSao)` WHERE `NgayTraThucTe IS NULL` | Kiểm tra sách chưa trả mà không quét toàn bảng. |

#### `PHAT`
| Index | Cột / INCLUDE / Filter | Lý do |
| --- | --- | --- |
| `IX_PHAT_IdPM_MaBanSao` | `(IdPM, MaBanSao)` | Phù hợp FK, JOIN nhanh từ `CT_PM`. |
| `IX_PHAT_TrangThai`, `IX_PHAT_TrangThai_NgayGhiNhan` | `(TrangThai)`, `(TrangThai, NgayGhiNhan DESC)` INCLUDE `(SoTien, LoaiPhat)` | Ưu tiên hiển thị phạt chưa đóng và sắp xếp theo ngày. |
| `IX_PHAT_NgayGhiNhan` | `(NgayGhiNhan DESC)` | Báo cáo theo mốc thời gian. |
| `IX_PHAT_ChuaDong` *(filtered)* | `(IdPM, NgayGhiNhan DESC)` INCLUDE `(SoTien, LoaiPhat)` WHERE `TrangThai = 'Chua dong'` | Rút ngắn quy trình thu phí tồn. |

#### `LICHLAM`
| Index | Cột / INCLUDE | Lý do |
| --- | --- | --- |
| `UX_LICHLAM_UniqueSlot` | `(EmailThuThu, Ngay, GioBatDau, GioKetThuc)` | Tránh trùng ca trước cả khi trigger kiểm tra. |
| `IX_LICHLAM_EmailThuThu` | `(EmailThuThu)` | Lấy toàn bộ ca của một thủ thư. |
| `IX_LICHLAM_EmailThuThu_Ngay` | `(EmailThuThu, Ngay)` INCLUDE `(GioBatDau, GioKetThuc, TrangThai, GhiChu)` | Lịch dạng timeline. |
| `IX_LICHLAM_TrangThai`, `IX_LICHLAM_Ngay`, `IX_LICHLAM_CreatedBy` | `(TrangThai)`, `(Ngay DESC)`, `(CreatedBy)` | Bộ lọc theo trạng thái/ngày/người phân công. |

#### `THONGBAO` & `THONGBAO_NGUOINHAN`
| Bảng | Index | Cột / Filter | Lý do |
| --- | --- | --- | --- |
| `THONGBAO` | `IX_THONGBAO_CreatedBy`, `IX_THONGBAO_CreatedAt` | `(CreatedBy)`, `(CreatedAt DESC)` | Theo dõi người gửi, sắp xếp tin mới nhất. |
| `THONGBAO_NGUOINHAN` | `IX_TBNN_EmailNhan_DaDoc` | `(EmailNhan, DaDoc)` INCLUDE `(IdThongBao, ReadAt)` | Đếm thông báo chưa đọc theo người nhận. |
|  | `IX_TBNN_IdThongBao` | `(IdThongBao)` | Liệt kê người nhận từng thông báo. |
|  | `IX_TBNN_EmailNhan_IdThongBao` *(filtered)* | `(EmailNhan, IdThongBao DESC)` WHERE `DaDoc = 0` | Phân trang hộp thư chưa đọc. |

#### `YEUCAU_RESETMK`
| Index | Cột / INCLUDE / Filter | Lý do |
| --- | --- | --- |
| `IX_YCRMK_EmailThuThu` | `(EmailThuThu)` | Lọc yêu cầu theo người gửi. |
| `IX_YCRMK_TrangThai_CreatedAt` | `(TrangThai, CreatedAt DESC)` INCLUDE `(EmailThuThu, LyDo)` | Theo dõi tồn đọng theo trạng thái. |
| `IX_YCRMK_XuLyBoi` | `(XuLyBoi)` | Audit người xử lý. |
| `IX_YCRMK_Pending` *(filtered)* | `(CreatedAt DESC)` INCLUDE `(EmailThuThu, LyDo)` WHERE `TrangThai = 'Pending'` | Truy vấn hàng chờ hiệu quả. |

## 3. Truy Cập Dữ Liệu Phức Tạp (DAO Layer)
- **`SachDAO.search` (Tìm Kiếm Nâng Cao)**: Triển khai công cụ tìm kiếm lai sử dụng Dynamic SQL. Kết hợp Full-Text Search của SQL Server (`CONTAINSTABLE` trên Tên sách/Tác giả) với bộ lọc quan hệ (Thể loại, NXB, Khoảng năm). Kết quả được xếp hạng theo `Score` tính toán và phân trang bằng `OFFSET-FETCH`.
- **`SachDAO.getAllForTable` (Phân Trang Keyset)**: Sử dụng **Keyset Pagination** (truy tìm theo `ISBN`) cho danh mục sách chính, đảm bảo hiệu năng O(1) bất kể độ sâu của trang, khắc phục nhược điểm chậm dần của phân trang `OFFSET` truyền thống.
- **`TaiKhoanDAO`**: Áp dụng **Keyset Pagination** cho quản lý tài khoản (`getAllAccounts`, `searchAccounts`) để xử lý danh sách người dùng lớn. Tích hợp `PasswordUtil` để xác thực BCrypt an toàn (`checkLogin`) và truy xuất thống kê hiệu suất nhân viên qua view `VW_TAIKHOAN_ProfileStats` (`getAccountProfile`).
- **`PhatDAO` (Phân Trang Con Trỏ Thủ Công)**: Thực hiện phân trang dựa trên con trỏ (`getAllPhatPaginated`) bằng cách lấy `N+1` dòng để kiểm tra trang kế tiếp. Logic ứng dụng tự tính toán `nextCursor`/`previousCursor`, tránh việc phải `COUNT(*)` tốn kém khi duyệt danh sách.
- **`PhieuMuonDAO.findCurrentBorrowed`**: JOIN `PHIEUMUON` ↔ `CT_PM` để liệt kê phiếu còn mở; tận dụng `IX_CT_PM_ChuaTra` và `IX_PHIEUMUON_IdBD_NgayMuon` khi lọc theo bạn đọc.
- **`ChiTietPhieuMuonDAO.getOverDue`**: Dùng composite index của `CT_PM` để nhận diện bản sao quá hạn mà không quét lịch sử.
- **`ThongKeDAO`**: Chứa các truy vấn tổng hợp (theo thể loại/tác giả/ngày/tháng) dựa mạnh vào index FK của `SACH`, `BANSAO`, `CT_PM` nhằm giữ GROUP BY trong bộ nhớ.

Tất cả DAO đều sử dụng PreparedStatement, giúp tái sử dụng execution plan trên SQL Server và ngăn SQL injection trong khi phát huy hiệu quả index.

## 4. Thiết Lập & Yêu Cầu
1. **Hệ quản trị**: Microsoft SQL Server 2019 trở lên (script dùng `GO`, filtered index, full-text catalog đặc thù SQL Server).
2. **Điều kiện tiên quyết**: Bật Full-Text Search, cấp quyền tạo database/catalog/trigger/view cho user thực thi.
3. **Thứ tự khởi tạo**:
	1. `create_tables.sql` – tạo `db_thuvien`, drop đối tượng cũ, định nghĩa bảng + constraint + trigger + view.
	2. `create_indexes.sql` – dựng toàn bộ B-tree/filtered/full-text sau khi bảng đã tồn tại.
	3. `insert_sample_data_1000.sql` – (tùy chọn) nạp dữ liệu mẫu cho demo và benchmark.
	4. `retrieval_queries.sql` hoặc chạy ứng dụng qua DAO – xác minh execution plan dùng index (`SET STATISTICS IO ON`).
	5. `reset_db.sql` + `clear_logs.sql` – tiện ích reset và dọn log khi thử nghiệm hiệu năng.
4. **Ví dụ chạy bằng `sqlcmd`**:
	```powershell
	sqlcmd -S .\SQLEXPRESS -i create_tables.sql
	sqlcmd -S .\SQLEXPRESS -i create_indexes.sql
	sqlcmd -S .\SQLEXPRESS -i insert_sample_data_1000.sql
	```
5. **Cấu hình DAO Java**: Cập nhật `QuanLyThuVien/src/main/resources/application.properties` với JDBC URL, driver SQL Server, thông tin xác thực và pool size trước khi khởi chạy backend.
