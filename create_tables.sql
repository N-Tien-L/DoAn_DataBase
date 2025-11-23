IF DB_ID('db_thuvien') IS NULL
BEGIN
    CREATE DATABASE db_thuvien;
END
GO

USE db_thuvien;
GO

-- Drop objects if they exist (idempotent create)
IF OBJECT_ID('PHAT','U') IS NOT NULL DROP TABLE PHAT;
IF OBJECT_ID('CT_PM','U') IS NOT NULL DROP TABLE CT_PM;
IF OBJECT_ID('PHIEUMUON','U') IS NOT NULL DROP TABLE PHIEUMUON;
IF OBJECT_ID('BANSAO','U') IS NOT NULL DROP TABLE BANSAO;
IF OBJECT_ID('SACH','U') IS NOT NULL DROP TABLE SACH;
IF OBJECT_ID('TACGIA','U') IS NOT NULL DROP TABLE TACGIA;
IF OBJECT_ID('THELOAI','U') IS NOT NULL DROP TABLE THELOAI;
IF OBJECT_ID('NHAXUATBAN','U') IS NOT NULL DROP TABLE NHAXUATBAN;
IF OBJECT_ID('BANDOC','U') IS NOT NULL DROP TABLE BANDOC;
IF OBJECT_ID('THONGBAO_NGUOINHAN','U') IS NOT NULL DROP TABLE THONGBAO_NGUOINHAN;
IF OBJECT_ID('THONGBAO','U') IS NOT NULL DROP TABLE THONGBAO;
IF OBJECT_ID('YEUCAU_RESETMK','U') IS NOT NULL DROP TABLE YEUCAU_RESETMK;
IF OBJECT_ID('LICHLAM','U') IS NOT NULL DROP TABLE LICHLAM;
IF OBJECT_ID('TAIKHOAN','U') IS NOT NULL DROP TABLE TAIKHOAN;
GO

-- =============================================
-- Bảng TAIKHOAN (mở rộng với metadata)
-- =============================================
CREATE TABLE TAIKHOAN (
    Email VARCHAR(50) PRIMARY KEY,
    [Password] VARCHAR(100) NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    [Role] VARCHAR(100) NOT NULL CHECK ([Role] IN ('Admin', 'ThuThu')) DEFAULT 'ThuThu',
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CreatedBy VARCHAR(50) NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'Active' CHECK (Status IN ('Active','Locked','Disabled')),
    CONSTRAINT FK_TAIKHOAN_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES TAIKHOAN(Email)
);
GO

-- =============================================
-- Bảng BANDOC (mở rộng với metadata)
-- =============================================
CREATE TABLE BANDOC (
    IdBD INT IDENTITY(1,1) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE,
    DiaChi NVARCHAR(255),
    SDT VARCHAR(15) UNIQUE,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CreatedBy VARCHAR(50) NULL,
    CONSTRAINT FK_BANDOC_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES TAIKHOAN(Email)
);
GO

-- =============================================
-- Bảng NHAXUATBAN
-- =============================================
CREATE TABLE NHAXUATBAN (
    MaNXB INT IDENTITY(1,1) PRIMARY KEY,
    TenNXB NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- =============================================
-- Bảng THELOAI
-- =============================================
CREATE TABLE THELOAI (
    MaTheLoai INT IDENTITY(1,1) PRIMARY KEY,
    TenTheLoai NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- =============================================
-- Bảng TACGIA
-- =============================================
CREATE TABLE TACGIA (
    MaTacGia INT IDENTITY(1,1),
    TenTacGia NVARCHAR(100) NOT NULL UNIQUE,
    Website VARCHAR(255),
    GhiChu NVARCHAR(MAX),
    CONSTRAINT PK_TACGIA PRIMARY KEY (MaTacGia)
);
GO

-- =============================================
-- Bảng SACH (thêm metadata tracking)
-- =============================================
CREATE TABLE SACH (
    ISBN VARCHAR(20),
    TenSach NVARCHAR(255) NOT NULL,
    MaTacGia INT,
    MaTheLoai INT,
    NamXuatBan INT,
    DinhDang NVARCHAR(20) NOT NULL DEFAULT N'Bản in',
    MoTa NVARCHAR(MAX),
    MaNXB INT,
    GiaBia DECIMAL(10, 2),
    SoLuongTon INT NOT NULL DEFAULT 0 CHECK (SoLuongTon >= 0),
    SoTrang INT,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CreatedBy VARCHAR(50) NULL,
    CONSTRAINT FK_SACH_THELOAI FOREIGN KEY (MaTheLoai) REFERENCES THELOAI(MaTheLoai) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT FK_SACH_NHAXUATBAN FOREIGN KEY (MaNXB) REFERENCES NHAXUATBAN(MaNXB) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT FK_SACH_TACGIA FOREIGN KEY (MaTacGia) REFERENCES TACGIA(MaTacGia) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT FK_SACH_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES TAIKHOAN(Email),
    CONSTRAINT PK_SACH PRIMARY KEY (ISBN)
);
GO

-- =============================================
-- Bảng BANSAO (thêm metadata tracking)
-- =============================================
CREATE TABLE BANSAO (
    MaBanSao INT IDENTITY(1,1) PRIMARY KEY,
    ISBN VARCHAR(20) NOT NULL,
    SoThuTuTrongKho INT NOT NULL,
    TinhTrang NVARCHAR(50) NOT NULL,
    NgayNhapKho DATE NOT NULL DEFAULT CAST(SYSUTCDATETIME() AS DATE),
    ViTriLuuTru VARCHAR(50),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CreatedBy VARCHAR(50) NULL,
    CONSTRAINT FK_BANSAO_SACH FOREIGN KEY (ISBN) REFERENCES SACH(ISBN) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT FK_BANSAO_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES TAIKHOAN(Email),
    CONSTRAINT UX_BANSAO UNIQUE (ISBN, SoThuTuTrongKho)
);
GO

-- =============================================
-- Bảng PHIEUMUON
-- =============================================
CREATE TABLE PHIEUMUON (
    IdPM INT IDENTITY(1,1) PRIMARY KEY,
    IdBD INT NOT NULL,
    EmailNguoiLap VARCHAR(50) NOT NULL,
    NgayMuon DATE NOT NULL DEFAULT CAST(SYSUTCDATETIME() AS DATE),
    HanTra DATE NOT NULL,
    CONSTRAINT FK_PM_BANDOC FOREIGN KEY (IdBD) REFERENCES BANDOC(IdBD) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT FK_PM_TAIKHOAN FOREIGN KEY (EmailNguoiLap) REFERENCES TAIKHOAN(Email),
    CONSTRAINT CK_HanTra_Sau_NgayMuon CHECK (HanTra >= NgayMuon)
);
GO

-- =============================================
-- Bảng CT_PM (EmailNguoiNhan tham chiếu TAIKHOAN)
-- =============================================
CREATE TABLE CT_PM (
    IdPM INT NOT NULL,
    MaBanSao INT NOT NULL,
    NgayTraThucTe DATE DEFAULT NULL,
    TinhTrangKhiTra NVARCHAR(50) DEFAULT NULL,
    EmailNguoiNhan VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (IdPM, MaBanSao),
    CONSTRAINT FK_CTPM_PM FOREIGN KEY (IdPM) REFERENCES PHIEUMUON(IdPM) ON DELETE CASCADE,
    CONSTRAINT FK_CTPM_BANSAO FOREIGN KEY (MaBanSao) REFERENCES BANSAO(MaBanSao) ON DELETE NO ACTION,
    CONSTRAINT FK_CTPM_EmailNguoiNhan FOREIGN KEY (EmailNguoiNhan) REFERENCES TAIKHOAN(Email)
);
GO

-- =============================================
-- Bảng PHAT
-- =============================================
CREATE TABLE PHAT (
    IdPhat INT IDENTITY(1,1) PRIMARY KEY,
    IdPM INT NOT NULL,
    MaBanSao INT NOT NULL,
    LoaiPhat VARCHAR(20) NOT NULL CHECK (LoaiPhat IN ('Tre han', 'Hong sach', 'Mat sach')),
    SoTien DECIMAL(10, 0) NOT NULL,
    NgayGhiNhan DATE NOT NULL DEFAULT CAST(SYSUTCDATETIME() AS DATE),
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'Chua dong' CHECK (TrangThai IN ('Chua dong', 'Da dong')),
    CONSTRAINT FK_PHAT_CTPM FOREIGN KEY (IdPM, MaBanSao) REFERENCES CT_PM(IdPM, MaBanSao)
);
GO

-- =============================================
-- Bảng LICHLAM (Phân công lịch làm việc)
-- =============================================
CREATE TABLE LICHLAM (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    EmailThuThu VARCHAR(50) NOT NULL,
    Ngay DATE NOT NULL,
    GioBatDau TIME(0) NOT NULL,
    GioKetThuc TIME(0) NOT NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'Scheduled' CHECK (TrangThai IN ('Scheduled','Done','Cancelled')),
    GhiChu NVARCHAR(255) NULL,
    CreatedBy VARCHAR(50) NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_LICHLAM_ThuThu FOREIGN KEY (EmailThuThu) REFERENCES TAIKHOAN(Email),
    CONSTRAINT FK_LICHLAM_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES TAIKHOAN(Email),
    CONSTRAINT CK_LICHLAM_Time CHECK (GioKetThuc > GioBatDau)
);
GO

-- Unique index: chống trùng ca làm y hệt
CREATE UNIQUE INDEX UX_LICHLAM_UniqueSlot ON LICHLAM (EmailThuThu, Ngay, GioBatDau, GioKetThuc);
GO

-- =============================================
-- Bảng THONGBAO (Hệ thống thông báo)
-- =============================================
CREATE TABLE THONGBAO (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    CreatedBy VARCHAR(50) NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_THONGBAO_CreatedBy FOREIGN KEY (CreatedBy) REFERENCES TAIKHOAN(Email)
);
GO

-- =============================================
-- Bảng THONGBAO_NGUOINHAN (Người nhận thông báo)
-- =============================================
CREATE TABLE THONGBAO_NGUOINHAN (
    IdThongBao INT NOT NULL,
    EmailNhan VARCHAR(50) NOT NULL,
    DaDoc BIT NOT NULL DEFAULT 0,
    ReadAt DATETIME2 NULL,
    PRIMARY KEY (IdThongBao, EmailNhan),
    CONSTRAINT FK_TBNN_ThongBao FOREIGN KEY (IdThongBao) REFERENCES THONGBAO(Id) ON DELETE CASCADE,
    CONSTRAINT FK_TBNN_NguoiNhan FOREIGN KEY (EmailNhan) REFERENCES TAIKHOAN(Email)
);
GO

-- =============================================
-- Bảng YEUCAU_RESETMK (Yêu cầu cấp lại mật khẩu)
-- =============================================
CREATE TABLE YEUCAU_RESETMK (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    EmailThuThu VARCHAR(50) NOT NULL,
    LyDo NVARCHAR(500) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (TrangThai IN ('Pending','Approved','Rejected','Done')),
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    XuLyBoi VARCHAR(50) NULL,
    XuLyLuc DATETIME2 NULL,
    GhiChuXuLy NVARCHAR(500) NULL,
    CONSTRAINT FK_YCRMK_ThuThu FOREIGN KEY (EmailThuThu) REFERENCES TAIKHOAN(Email),
    CONSTRAINT FK_YCRMK_XuLyBoi FOREIGN KEY (XuLyBoi) REFERENCES TAIKHOAN(Email)
);
GO

-- =============================================
-- VIEWS: Thống kê hồ sơ tài khoản
-- =============================================
IF OBJECT_ID('VW_TAIKHOAN_ProfileStats','V') IS NOT NULL DROP VIEW VW_TAIKHOAN_ProfileStats;
GO

CREATE VIEW VW_TAIKHOAN_ProfileStats AS
SELECT
    t.Email,
    t.HoTen,
    t.[Role],
    t.Status,
    t.CreatedAt,
    t.CreatedBy,
    -- Số bạn đọc do họ tạo
    (SELECT COUNT(*) FROM BANDOC b WHERE b.CreatedBy = t.Email) AS SoBanDocTao,
    -- Số sách do họ thêm vào hệ thống
    (SELECT COUNT(*) FROM SACH s WHERE s.CreatedBy = t.Email) AS SoSachThem,
    -- Số bản sao do họ nhập kho
    (SELECT COUNT(*) FROM BANSAO bs WHERE bs.CreatedBy = t.Email) AS SoBanSaoNhap,
    -- Số phiếu mượn do họ lập
    (SELECT COUNT(*) FROM PHIEUMUON pm WHERE pm.EmailNguoiLap = t.Email) AS SoPhieuMuonLap,
    -- Số bản sao đang cho mượn (CT_PM chưa trả) do họ lập phiếu
    (SELECT COUNT(*) 
     FROM CT_PM ct 
     JOIN PHIEUMUON pm ON pm.IdPM = ct.IdPM
     WHERE pm.EmailNguoiLap = t.Email AND ct.NgayTraThucTe IS NULL) AS SoBanSaoDangChoMuon
FROM TAIKHOAN t;
GO

-- =============================================
-- TRIGGERS
-- =============================================

-- Trigger: Kiểm tra sách bản in phải có số trang
IF OBJECT_ID('TRG_SACH_Insert_Update','TR') IS NOT NULL DROP TRIGGER TRG_SACH_Insert_Update;
GO
CREATE TRIGGER TRG_SACH_Insert_Update
ON SACH
AFTER INSERT, UPDATE
AS
BEGIN
    IF EXISTS (
        SELECT 1
        FROM inserted
        WHERE DinhDang = N'Bản in' AND (SoTrang IS NULL OR SoTrang <= 0)
    )
    BEGIN
        RAISERROR (N'Sách có định dạng "Bản in" phải có Số Trang lớn hơn 0.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

-- Trigger: Khi trả sách phải có tình trạng
IF OBJECT_ID('TRG_CT_PM_Update','TR') IS NOT NULL DROP TRIGGER TRG_CT_PM_Update;
GO
CREATE TRIGGER TRG_CT_PM_Update
ON CT_PM
AFTER UPDATE
AS
BEGIN
    IF UPDATE(NgayTraThucTe)
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM inserted
            WHERE NgayTraThucTe IS NOT NULL AND TinhTrangKhiTra IS NULL
        )
        BEGIN
            RAISERROR (N'Khi có Ngày trả thực tế, Tình trạng khi trả không được NULL.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
    END
END;
GO

-- Trigger: Cập nhật SoLuongTon khi thêm/xóa bản sao
IF OBJECT_ID('TRG_BANSAO_Update_SoLuongTon','TR') IS NOT NULL DROP TRIGGER TRG_BANSAO_Update_SoLuongTon;
GO
CREATE TRIGGER TRG_BANSAO_Update_SoLuongTon
ON BANSAO
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE SACH
    SET SoLuongTon = (
        SELECT COUNT(*)
        FROM BANSAO
        WHERE BANSAO.ISBN = SACH.ISBN
    )
    WHERE SACH.ISBN IN (
        SELECT ISBN FROM inserted
        UNION
        SELECT ISBN FROM deleted
    );
END;
GO

-- Trigger: Chống chồng lấn ca làm việc
IF OBJECT_ID('TRG_LICHLAM_NoOverlap','TR') IS NOT NULL DROP TRIGGER TRG_LICHLAM_NoOverlap;
GO
CREATE TRIGGER TRG_LICHLAM_NoOverlap
ON LICHLAM
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM LICHLAM l
        JOIN inserted i ON l.EmailThuThu = i.EmailThuThu AND l.Ngay = i.Ngay
        WHERE l.Id <> i.Id
          AND NOT (l.GioKetThuc <= i.GioBatDau OR i.GioKetThuc <= l.GioBatDau)
    )
    BEGIN
        RAISERROR (N'Ca làm bị chồng lấn cho cùng thủ thư trong cùng ngày.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO

PRINT N'===================================================';
PRINT 'DATABASE HAS BEEN INITIALIZED SUCCESSFULLY'
PRINT N'===================================================';
GO