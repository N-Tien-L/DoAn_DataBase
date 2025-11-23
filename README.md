[**🇻🇳 Tiếng Việt**](README.vi.md)

# Library Database Backend Documentation

## 1. Database Architecture & Schema Design

### Entity Overview
| Table | Description | Key Relationships |
| --- | --- | --- |
| `TAIKHOAN` | Staff accounts with role-based access, lifecycle metadata, and optional creator audit. | Self-referencing FK `CreatedBy` enforces delegated provisioning. |
| `BANDOC` | Patrons with contact info and audit trail. | FK `CreatedBy → TAIKHOAN`; referenced by `PHIEUMUON`. |
| `NHAXUATBAN`, `THELOAI`, `TACGIA` | Dimension tables for catalog metadata. | Referenced by `SACH` via FK constraints. |
| `SACH` | Canonical book records including format, price, availability counters, and authorship. | FK to `TACGIA`, `THELOAI`, `NHAXUATBAN`, `TAIKHOAN`; parent of `BANSAO`. |
| `BANSAO` | Physical copies with inventory metadata and storage location. | FK `ISBN → SACH`; participates in `CT_PM` and `PHAT`. |
| `PHIEUMUON` | Borrowing slips initiated by librarians. | FK `IdBD → BANDOC`, `EmailNguoiLap → TAIKHOAN`; parent of `CT_PM`. |
| `CT_PM` | Line items of each borrowing transaction. | Composite PK `(IdPM, MaBanSao)` plus FK to `PHIEUMUON`, `BANSAO`, `TAIKHOAN`. |
| `PHAT` | Fine ledger entries tied to unreturned/damaged copies. | FK `(IdPM, MaBanSao) → CT_PM`. |
| `LICHLAM` | Staff scheduling table with overlap prevention. | FK `EmailThuThu` and `CreatedBy` → `TAIKHOAN`. |
| `THONGBAO`, `THONGBAO_NGUOINHAN` | Notification broadcaster and recipient mapping. | Cascade FK `Id → THONGBAO`; FK `EmailNhan → TAIKHOAN`. |
| `YEUCAU_RESETMK` | Password reset workflow with handler audit trail. | FK to `TAIKHOAN` for requester and resolver metadata. |

### Relationship Narrative (ERD Summary)
The schema represents a classical library domain: `TAIKHOAN` (admins/librarians) curate entities (`SACH`, `BANSAO`, `BANDOC`) and drive workflows (`PHIEUMUON` → `CT_PM` → `PHAT`). Dimension tables (`THELOAI`, `NHAXUATBAN`, `TACGIA`) normalize descriptive attributes. Notifications and schedules extend operations, while password reset requests and fines add governance. All foreign keys use `ON UPDATE CASCADE` where appropriate to maintain referential consistency without cascading deletes that could erase operational history.

### Design Considerations
- **3NF + audit columns**: Each entity isolates its attributes, while `CreatedAt`/`CreatedBy` columns supply lineage for reporting (`VW_TAIKHOAN_ProfileStats`).
- **Defensive constraints**: CHECK constraints validate enums (roles, statuses, penalty types) and data quality (non-negative inventory, due date ≥ loan date).
- **Business logic in triggers**:
  - `TRG_SACH_Insert_Update` enforces that physical books specify page counts.
  - `TRG_CT_PM_Update` ensures a condition is recorded when a return date is captured.
  - `TRG_BANSAO_Update_SoLuongTon` keeps `SACH.SoLuongTon` synchronized with the count of copies.
  - `TRG_LICHLAM_NoOverlap` guarantees shift slots never overlap per librarian/day.

### Supporting Views & Features
- `VW_TAIKHOAN_ProfileStats` materializes operational KPIs per staff account (patrons created, copies registered, active loans).
- `UX_LICHLAM_UniqueSlot` and other uniqueness constraints prevent duplicate scheduling/inventory anomalies.
- Full-text catalog `LibraryCatalog` accelerates Vietnamese language searches across `TACGIA.TenTacGia` and `SACH.TenSach`.

## 2. **Performance Optimization & Indexing Strategy (CRITICAL)**
All indexes live in `create_indexes.sql`. The design balances OLTP writes with reporting workloads by combining foreign-key helpers, selective filtered indexes, and covering composites.

#### `TAIKHOAN`
| Index | Columns / Filter | Rationale |
| --- | --- | --- |
| `IX_TAIKHOAN_CreatedBy` | `(CreatedBy)` WHERE `CreatedBy IS NOT NULL` | Supports the view counting who provisioned which records without bloating the index with self-managed accounts. |

#### `BANDOC`
| Index | Columns | Rationale |
| --- | --- | --- |
| `IX_BANDOC_CreatedBy` | `(CreatedBy)` | Fast aggregation of patrons created per staff member. |
| `IX_BANDOC_CreatedAt` | `(CreatedAt DESC)` | Drives timeline analytics and rolling onboard reports. |

#### `SACH`
| Index | Columns / Include | Rationale |
| --- | --- | --- |
| `IX_SACH_MaTacGia`, `IX_SACH_MaTheLoai`, `IX_SACH_MaNXB` | `(MaTacGia)`, `(MaTheLoai)`, `(MaNXB)` | Accelerate joins from lookup dimensions and cascading updates. |
| `IX_SACH_NamXuatBan` | `(NamXuatBan)` | Speeds catalog browsing by publication year. |
| `IX_SACH_TenSach` | `(TenSach)` INCLUDE `(ISBN, SoLuongTon)` | Supports prefix searches while covering inventory columns to avoid bookmark lookups. |
| `IX_SACH_TheLoai_TenSach` | `(MaTheLoai, TenSach)` | Matches UI filters showing titles grouped by category with alphabetical sorting. |
| `IX_SACH_CreatedBy`, `IX_SACH_CreatedAt` | `(CreatedBy)`, `(CreatedAt DESC)` | Feed curator KPIs and recent acquisitions dashboards. |
| **Full-text** `LibraryCatalog` | `TenSach` | Provides linguistic search capabilities beyond B-tree LIKE predicates. |

#### `BANSAO`
| Index | Columns / Include / Filter | Rationale |
| --- | --- | --- |
| `IX_BANSAO_ISBN` | `(ISBN)` | Foreign-key support for joins from copies back to master books. |
| `IX_BANSAO_TinhTrang` | `(TinhTrang)` | Rapid filtering by availability status. |
| `IX_BANSAO_ISBN_TinhTrang` | `(ISBN, TinhTrang)` INCLUDE `(MaBanSao, ViTriLuuTru)` | Frequent query: “find available copy + location” for a title. |
| `IX_BANSAO_ViTriLuuTru` | `(ViTriLuuTru)` | Warehouse slot lookups. |
| `IX_BANSAO_CreatedBy`, `IX_BANSAO_CreatedAt`, `IX_BANSAO_NgayNhapKho` | `(CreatedBy)`, `(CreatedAt DESC)`, `(NgayNhapKho DESC)` | Inventory intake analytics. |
| `IX_BANSAO_Available` *(filtered)* | `(ISBN)` WHERE `TinhTrang = N'Có sẵn'` | Keeps the “available copies” search narrow even when historical states accumulate. |

#### `PHIEUMUON`
| Index | Columns / Include | Rationale |
| --- | --- | --- |
| `IX_PHIEUMUON_IdBD` | `(IdBD)` | Patron loan history retrieval. |
| `IX_PHIEUMUON_EmailNguoiLap` | `(EmailNguoiLap)` | Audits per librarian. |
| `IX_PHIEUMUON_NgayMuon`, `IX_PHIEUMUON_HanTra` | `(NgayMuon DESC)`, `(HanTra)` | Timeline filters and overdue scans. |
| `IX_PHIEUMUON_IdBD_NgayMuon` | `(IdBD, NgayMuon DESC)` INCLUDE `(HanTra)` | Composite covering query for patron statements grouped by loan date with due date display. |

#### `CT_PM`
| Index | Columns / Include / Filter | Rationale |
| --- | --- | --- |
| `IX_CT_PM_MaBanSao` | `(MaBanSao)` | Quickly determine which transaction holds a given copy. |
| `IX_CT_PM_NgayTraThucTe` | `(NgayTraThucTe)` | Drives return status dashboards. |
| `IX_CT_PM_EmailNguoiNhan` | `(EmailNguoiNhan)` | Tracks handoffs between circulation staff. |
| `IX_CT_PM_IdPM_NgayTraThucTe` | `(IdPM, NgayTraThucTe)` INCLUDE `(MaBanSao, TinhTrangKhiTra)` | Answers “which items in loan X are still outstanding and in what condition.” |
| `IX_CT_PM_ChuaTra` *(filtered)* | `(IdPM, MaBanSao)` WHERE `NgayTraThucTe IS NULL` | Eliminates full scans when checking unsettled items. |

#### `PHAT`
| Index | Columns / Include / Filter | Rationale |
| --- | --- | --- |
| `IX_PHAT_IdPM_MaBanSao` | `(IdPM, MaBanSao)` | Aligned with FK for cascaded lookups from loan lines. |
| `IX_PHAT_TrangThai`, `IX_PHAT_TrangThai_NgayGhiNhan` | `(TrangThai)`, `(TrangThai, NgayGhiNhan DESC)` INCLUDE `(SoTien, LoaiPhat)` | Surface unpaid fines first and provide sorted ledgers for billing. |
| `IX_PHAT_NgayGhiNhan` | `(NgayGhiNhan DESC)` | Chronological reporting. |
| `IX_PHAT_ChuaDong` *(filtered)* | `(IdPM, NgayGhiNhan DESC)` INCLUDE `(SoTien, LoaiPhat)` WHERE `TrangThai = 'Chua dong'` | Accelerates the “collect outstanding fines” workflow. |

#### `LICHLAM`
| Index | Columns / Include | Rationale |
| --- | --- | --- |
| `UX_LICHLAM_UniqueSlot` | `(EmailThuThu, Ngay, GioBatDau, GioKetThuc)` | Guarantees uniqueness before trigger validation. |
| `IX_LICHLAM_EmailThuThu` | `(EmailThuThu)` | Retrieve all shifts for a librarian. |
| `IX_LICHLAM_EmailThuThu_Ngay` | `(EmailThuThu, Ngay)` INCLUDE `(GioBatDau, GioKetThuc, TrangThai, GhiChu)` | Supports calendar views for a date range. |
| `IX_LICHLAM_TrangThai`, `IX_LICHLAM_Ngay`, `IX_LICHLAM_CreatedBy` | `(TrangThai)`, `(Ngay DESC)`, `(CreatedBy)` | Operational dashboards by state/day/dispatcher. |

#### `THONGBAO` & `THONGBAO_NGUOINHAN`
| Table | Index | Columns / Filter | Rationale |
| --- | --- | --- | --- |
| `THONGBAO` | `IX_THONGBAO_CreatedBy`, `IX_THONGBAO_CreatedAt` | `(CreatedBy)`, `(CreatedAt DESC)` | Trace who broadcast notices and show newest messages first. |
| `THONGBAO_NGUOINHAN` | `IX_TBNN_EmailNhan_DaDoc` | `(EmailNhan, DaDoc)` INCLUDE `(IdThongBao, ReadAt)` | Powers inbox unread counters. |
|  | `IX_TBNN_IdThongBao` | `(IdThongBao)` | Quickly list recipients per announcement. |
|  | `IX_TBNN_EmailNhan_IdThongBao` *(filtered)* | `(EmailNhan, IdThongBao DESC)` WHERE `DaDoc = 0` | Cursor pagination over unread notifications. |

#### `YEUCAU_RESETMK`
| Index | Columns / Include / Filter | Rationale |
| --- | --- | --- |
| `IX_YCRMK_EmailThuThu` | `(EmailThuThu)` | Fetch all requests per librarian. |
| `IX_YCRMK_TrangThai_CreatedAt` | `(TrangThai, CreatedAt DESC)` INCLUDE `(EmailThuThu, LyDo)` | Supervisors monitor backlog by status in order of recency. |
| `IX_YCRMK_XuLyBoi` | `(XuLyBoi)` | Audit who processed which tickets. |
| `IX_YCRMK_Pending` *(filtered)* | `(CreatedAt DESC)` INCLUDE `(EmailThuThu, LyDo)` WHERE `TrangThai = 'Pending'` | Keeps the “Pending queue” lean and CPU-friendly.

## 3. Complex Data Access (DAO Layer)
- **`SachDAO.search` (Advanced Search)**: Implements a hybrid search engine using Dynamic SQL. It combines SQL Server's Full-Text Search (`CONTAINSTABLE` on Title/Author) with relational filtering (Category, Publisher, Year Range). Results are ranked by a computed relevance `Score` and paginated using `OFFSET-FETCH`.
- **`SachDAO.getAllForTable` (Keyset Pagination)**: Uses **Keyset Pagination** (seeking by `ISBN`) for the main catalog view, ensuring O(1) performance regardless of page depth, unlike traditional `OFFSET` pagination which degrades linearly.
- **`TaiKhoanDAO`**: Implements **Keyset Pagination** for account management (`getAllAccounts`, `searchAccounts`) to handle large user bases efficiently. It also integrates with `PasswordUtil` for secure BCrypt verification (`checkLogin`) and retrieves comprehensive staff statistics via `VW_TAIKHOAN_ProfileStats` (`getAccountProfile`).
- **`PhatDAO` (Manual Cursor Pagination)**: Implements cursor-based pagination (`getAllPhatPaginated`) by fetching `N+1` rows to look ahead. It calculates `nextCursor`/`previousCursor` in application logic to avoid expensive `COUNT(*)` operations during traversal.
- **`PhieuMuonDAO.findCurrentBorrowed`**: Joins `PHIEUMUON` ↔ `CT_PM` to list active loans. It fully benefits from `IX_CT_PM_ChuaTra` and `IX_PHIEUMUON_IdBD_NgayMuon` when filtering per patron.
- **`ChiTietPhieuMuonDAO.getOverDue`**: Leverages composite indexes on `CT_PM` to isolate overdue copies without scanning historical rows.
- **`ThongKeDAO`**: Contains aggregation-heavy queries (borrow counts by genre/author/day/month). They depend on FK indexes across `SACH`, `BANSAO`, `CT_PM`, and filtered indexes to keep GROUP BY operations in-memory.

These DAOs consistently rely on prepared statements, preserving plan cache reuse on SQL Server and sidestepping injection risks while exploiting the indexing strategy above.

## 4. Setup & Requirements
1. **Database Engine**: Microsoft SQL Server 2019+ (scripts use `GO`, `sys.indexes`, filtered indexes, and full-text catalogs specific to SQL Server).
2. **Prerequisites**: Enable Full-Text Search feature, and ensure the executing login has rights to create databases, catalogs, triggers, and views.
3. **Initialization Order**:
	1. `create_tables.sql` – creates `db_thuvien`, drops stale objects, defines tables, constraints, triggers, and the reporting view.
	2. `create_indexes.sql` – builds b-tree, filtered, unique, and full-text indexes after base objects exist.
	3. `insert_sample_data_1000.sql` – (optional) seeds test data for demos/perf tests.
	4. `retrieval_queries.sql` or DAO-backed application flows – validate that indexes are used (`SET STATISTICS IO ON`).
	5. `reset_db.sql` + `clear_logs.sql` – utility scripts for teardown or log cleanup during benchmarking.
4. **Execution Example (sqlcmd)**:
	```powershell
	sqlcmd -S .\SQLEXPRESS -i create_tables.sql
	sqlcmd -S .\SQLEXPRESS -i create_indexes.sql
	sqlcmd -S .\SQLEXPRESS -i insert_sample_data_1000.sql
	```
5. **Java DAO Configuration**: Update `QuanLyThuVien/src/main/resources/application.properties` with the correct JDBC URL, SQL Server driver, credentials, and connection pool sizing before running the Spring Boot (or plain JDBC) backend.
