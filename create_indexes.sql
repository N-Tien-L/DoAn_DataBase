USE db_thuvien;
GO

-- =============================================
-- INDEXES CHO TỐI ƯU HÓA HIỆU SUẤT
-- =============================================

-- =============================================
-- Bảng TAIKHOAN
-- =============================================

-- Index cho CreatedBy (hỗ trợ VIEW VW_TAIKHOAN_ProfileStats đếm "ai tạo bao nhiêu tài khoản")
CREATE NONCLUSTERED INDEX IX_TAIKHOAN_CreatedBy 
ON TAIKHOAN(CreatedBy)
WHERE CreatedBy IS NOT NULL;  -- Filtered index: chỉ index khi có CreatedBy
GO

-- =============================================
-- Bảng BANDOC
-- =============================================

-- Index cho CreatedBy (thống kê: thủ thư nào tạo bao nhiêu bạn đọc)
CREATE NONCLUSTERED INDEX IX_BANDOC_CreatedBy 
ON BANDOC(CreatedBy);
GO

-- Index cho CreatedAt (thống kê theo thời gian)
CREATE NONCLUSTERED INDEX IX_BANDOC_CreatedAt 
ON BANDOC(CreatedAt DESC);
GO

-- =============================================
-- Bảng SACH
-- =============================================

-- Index cho Foreign Keys (tối ưu JOIN operations)
CREATE NONCLUSTERED INDEX IX_SACH_MaTacGia 
ON SACH(MaTacGia);
GO

CREATE NONCLUSTERED INDEX IX_SACH_MaTheLoai 
ON SACH(MaTheLoai);
GO

CREATE NONCLUSTERED INDEX IX_SACH_MaNXB 
ON SACH(MaNXB);
GO

-- Index cho cột thường dùng để lọc/thống kê
CREATE NONCLUSTERED INDEX IX_SACH_NamXuatBan 
ON SACH(NamXuatBan);
GO

-- Index cho tìm kiếm theo tên sách
-- Bao gồm ISBN để tránh lookup operation
CREATE NONCLUSTERED INDEX IX_SACH_TenSach 
ON SACH(TenSach) 
INCLUDE (ISBN, SoLuongTon);
GO

-- Composite index cho query phổ biến: lọc theo thể loại và sắp xếp theo tên
CREATE NONCLUSTERED INDEX IX_SACH_TheLoai_TenSach 
ON SACH(MaTheLoai, TenSach);
GO

-- Index cho CreatedBy (thống kê: thủ thư nào thêm bao nhiêu sách)
CREATE NONCLUSTERED INDEX IX_SACH_CreatedBy 
ON SACH(CreatedBy);
GO

-- Index cho CreatedAt (thống kê theo thời gian)
CREATE NONCLUSTERED INDEX IX_SACH_CreatedAt 
ON SACH(CreatedAt DESC);
GO

-- =============================================
-- Bảng BANSAO
-- =============================================

-- Index cho Foreign Key
CREATE NONCLUSTERED INDEX IX_BANSAO_ISBN 
ON BANSAO(ISBN);
GO

-- Index cho cột tình trạng (thường dùng để lọc sách còn/hết)
CREATE NONCLUSTERED INDEX IX_BANSAO_TinhTrang 
ON BANSAO(TinhTrang);
GO

-- Composite index cho query phổ biến: tìm bản sao có sẵn của một cuốn sách
CREATE NONCLUSTERED INDEX IX_BANSAO_ISBN_TinhTrang 
ON BANSAO(ISBN, TinhTrang)
INCLUDE (MaBanSao, ViTriLuuTru);
GO

-- Index cho vị trí lưu trữ (hỗ trợ tìm kiếm sách theo kho)
CREATE NONCLUSTERED INDEX IX_BANSAO_ViTriLuuTru 
ON BANSAO(ViTriLuuTru);
GO

-- Index cho CreatedBy (thống kê: thủ thư nào nhập bao nhiêu bản sao)
CREATE NONCLUSTERED INDEX IX_BANSAO_CreatedBy 
ON BANSAO(CreatedBy);
GO

-- Index cho CreatedAt (thống kê theo thời gian)
CREATE NONCLUSTERED INDEX IX_BANSAO_CreatedAt 
ON BANSAO(CreatedAt DESC);
GO

-- Index cho NgayNhapKho (thống kê nhập kho theo thời gian)
CREATE NONCLUSTERED INDEX IX_BANSAO_NgayNhapKho 
ON BANSAO(NgayNhapKho DESC);
GO

-- =============================================
-- Bảng PHIEUMUON
-- =============================================

-- Index cho Foreign Key IdBD
CREATE NONCLUSTERED INDEX IX_PHIEUMUON_IdBD 
ON PHIEUMUON(IdBD);
GO

-- Index cho Foreign Key EmailNguoiLap
CREATE NONCLUSTERED INDEX IX_PHIEUMUON_EmailNguoiLap 
ON PHIEUMUON(EmailNguoiLap);
GO

-- Index cho ngày mượn (thống kê theo thời gian)
CREATE NONCLUSTERED INDEX IX_PHIEUMUON_NgayMuon 
ON PHIEUMUON(NgayMuon DESC);
GO

-- Index cho hạn trả (tìm phiếu quá hạn)
CREATE NONCLUSTERED INDEX IX_PHIEUMUON_HanTra 
ON PHIEUMUON(HanTra);
GO

-- Composite index cho query lịch sử mượn của bạn đọc
CREATE NONCLUSTERED INDEX IX_PHIEUMUON_IdBD_NgayMuon 
ON PHIEUMUON(IdBD, NgayMuon DESC)
INCLUDE (HanTra);
GO

-- =============================================
-- Bảng CT_PM (Chi tiết Phiếu Mượn)
-- =============================================

-- Index cho Foreign Key MaBanSao (IdPM đã có trong composite PK)
CREATE NONCLUSTERED INDEX IX_CT_PM_MaBanSao 
ON CT_PM(MaBanSao);
GO

-- Index cho ngày trả thực tế (kiểm tra sách đã trả chưa)
CREATE NONCLUSTERED INDEX IX_CT_PM_NgayTraThucTe 
ON CT_PM(NgayTraThucTe);
GO

-- Index cho EmailNguoiNhan
CREATE NONCLUSTERED INDEX IX_CT_PM_EmailNguoiNhan 
ON CT_PM(EmailNguoiNhan);
GO

-- Composite index cho tìm sách chưa trả (query phổ biến)
CREATE NONCLUSTERED INDEX IX_CT_PM_IdPM_NgayTraThucTe 
ON CT_PM(IdPM, NgayTraThucTe)
INCLUDE (MaBanSao, TinhTrangKhiTra);
GO

-- =============================================
-- Bảng PHAT
-- =============================================

-- Index cho Foreign Key composite (IdPM, MaBanSao)
CREATE NONCLUSTERED INDEX IX_PHAT_IdPM_MaBanSao 
ON PHAT(IdPM, MaBanSao);
GO

-- Index cho trạng thái phạt (tìm phạt chưa đóng)
CREATE NONCLUSTERED INDEX IX_PHAT_TrangThai 
ON PHAT(TrangThai);
GO

-- Index cho ngày ghi nhận (thống kê theo thời gian)
CREATE NONCLUSTERED INDEX IX_PHAT_NgayGhiNhan 
ON PHAT(NgayGhiNhan DESC);
GO

-- Composite index cho query phổ biến: tìm phạt chưa đóng và sắp xếp theo ngày
CREATE NONCLUSTERED INDEX IX_PHAT_TrangThai_NgayGhiNhan 
ON PHAT(TrangThai, NgayGhiNhan DESC)
INCLUDE (SoTien, LoaiPhat);
GO

-- =============================================
-- Bảng LICHLAM
-- =============================================

-- Index cho EmailThuThu (xem lịch của một thủ thư)
CREATE NONCLUSTERED INDEX IX_LICHLAM_EmailThuThu 
ON LICHLAM(EmailThuThu);
GO

-- Composite index cho query phổ biến: xem lịch của thủ thư theo khoảng ngày
CREATE NONCLUSTERED INDEX IX_LICHLAM_EmailThuThu_Ngay 
ON LICHLAM(EmailThuThu, Ngay)
INCLUDE (GioBatDau, GioKetThuc, TrangThai, GhiChu);
GO

-- Index cho filter theo TrangThai (Scheduled/Done/Cancelled)
CREATE NONCLUSTERED INDEX IX_LICHLAM_TrangThai 
ON LICHLAM(TrangThai);
GO

-- Index cho Ngay (xem tất cả ca làm trong một ngày)
CREATE NONCLUSTERED INDEX IX_LICHLAM_Ngay 
ON LICHLAM(Ngay DESC);
GO

-- Index cho CreatedBy (xem admin nào phân công ca)
CREATE NONCLUSTERED INDEX IX_LICHLAM_CreatedBy 
ON LICHLAM(CreatedBy);
GO

-- =============================================
-- Bảng THONGBAO
-- =============================================

-- Index cho CreatedBy (xem ai gửi thông báo)
CREATE NONCLUSTERED INDEX IX_THONGBAO_CreatedBy 
ON THONGBAO(CreatedBy);
GO

-- Index cho CreatedAt (sắp xếp thông báo theo thời gian)
CREATE NONCLUSTERED INDEX IX_THONGBAO_CreatedAt 
ON THONGBAO(CreatedAt DESC);
GO

-- =============================================
-- Bảng THONGBAO_NGUOINHAN
-- =============================================

-- Composite index quan trọng: hộp thư của người dùng, filter chưa đọc
CREATE NONCLUSTERED INDEX IX_TBNN_EmailNhan_DaDoc 
ON THONGBAO_NGUOINHAN(EmailNhan, DaDoc)
INCLUDE (IdThongBao, ReadAt);
GO

-- Index cho IdThongBao (xem ai nhận thông báo nào)
CREATE NONCLUSTERED INDEX IX_TBNN_IdThongBao 
ON THONGBAO_NGUOINHAN(IdThongBao);
GO

-- Composite index cho cursor pagination: Email + Id cursor
CREATE NONCLUSTERED INDEX IX_TBNN_EmailNhan_IdThongBao 
ON THONGBAO_NGUOINHAN(EmailNhan, IdThongBao DESC)
WHERE DaDoc = 0;  -- Filtered index: chỉ index thông báo chưa đọc
GO

-- =============================================
-- Bảng YEUCAU_RESETMK
-- =============================================

-- Index cho EmailThuThu (xem yêu cầu của một thủ thư)
CREATE NONCLUSTERED INDEX IX_YCRMK_EmailThuThu 
ON YEUCAU_RESETMK(EmailThuThu);
GO

-- Composite index quan trọng: admin xem yêu cầu theo trạng thái + sắp xếp
CREATE NONCLUSTERED INDEX IX_YCRMK_TrangThai_CreatedAt 
ON YEUCAU_RESETMK(TrangThai, CreatedAt DESC)
INCLUDE (EmailThuThu, LyDo);
GO

-- Index cho XuLyBoi (xem admin nào đã xử lý)
CREATE NONCLUSTERED INDEX IX_YCRMK_XuLyBoi 
ON YEUCAU_RESETMK(XuLyBoi);
GO

-- Filtered index: chỉ index yêu cầu đang pending (hay dùng nhất)
CREATE NONCLUSTERED INDEX IX_YCRMK_Pending 
ON YEUCAU_RESETMK(CreatedAt DESC)
INCLUDE (EmailThuThu, LyDo)
WHERE TrangThai = 'Pending';
GO

-- =============================================
-- FILTERED INDEX (Index có điều kiện)
-- =============================================

-- Index cho tìm bản sao có sẵn (chỉ index những bản sao có sẵn)
CREATE NONCLUSTERED INDEX IX_BANSAO_Available 
ON BANSAO(ISBN)
WHERE TinhTrang = N'Có sẵn';
GO

-- Index cho tìm sách chưa trả
CREATE NONCLUSTERED INDEX IX_CT_PM_ChuaTra 
ON CT_PM(IdPM, MaBanSao)
WHERE NgayTraThucTe IS NULL;
GO

-- Index cho tìm phạt chưa đóng
CREATE NONCLUSTERED INDEX IX_PHAT_ChuaDong 
ON PHAT(IdPM, NgayGhiNhan DESC)
INCLUDE (SoTien, LoaiPhat)
WHERE TrangThai = 'Chua dong';
GO

PRINT N'=====================================================';
PRINT N'INDEXES CREATED SUCCESSFULLY!';
PRINT N'=====================================================';
GO

-- =============================================
-- FULLTEXT INDEX (Index tìm kiếm toàn văn bản)
-- =============================================

-- Tạo catalog tên là 'LibraryCatalog'
CREATE FULLTEXT CATALOG LibraryCatalog
WITH ACCENT_SENSITIVITY = OFF; -- Tắt nhạy dấu ngay từ đầu
GO

CREATE FULLTEXT INDEX ON TACGIA
(
    TenTacGia LANGUAGE 1066 -- 1066 là mã ngôn ngữ Tiếng Việt (Vietnamese)
)
KEY INDEX PK_TACGIA
ON LibraryCatalog;
GO

CREATE FULLTEXT INDEX ON SACH
(
    TenSach LANGUAGE 1066,
)
KEY INDEX PK_SACH
ON LibraryCatalog;
GO

-- =============================================
-- SCRIPT KIỂM TRA INDEX
-- =============================================

-- Xem tất cả các index trong database
SELECT 
    OBJECT_NAME(i.object_id) AS TableName,
    i.name AS IndexName,
    i.type_desc AS IndexType,
    i.is_unique AS IsUnique,
    i.filter_definition AS FilterDefinition,
    STUFF((
        SELECT ', ' + c.name
        FROM sys.index_columns ic
        JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
        WHERE ic.object_id = i.object_id AND ic.index_id = i.index_id
        ORDER BY ic.key_ordinal
        FOR XML PATH('')
    ), 1, 2, '') AS IndexedColumns
FROM sys.indexes i
WHERE i.object_id IN (
    SELECT object_id 
    FROM sys.tables 
    WHERE name IN ('SACH', 'BANSAO', 'PHIEUMUON', 'CT_PM', 'PHAT', 'BANDOC', 'TACGIA')
)
AND i.type > 0 -- Loại bỏ HEAP
ORDER BY TableName, IndexName;
GO
