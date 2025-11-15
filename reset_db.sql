/*
============================================================
 SCRIPT RESET DATABASE (XÓA TOÀN BỘ DỮ LIỆU VÀ TÁI TẠO)
============================================================
*/

USE master;
GO

-- Đóng tất cả connection hiện tại
ALTER DATABASE db_thuvien SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO

-- Xóa database cũ
DROP DATABASE IF EXISTS db_thuvien;
GO

-- Tạo database mới
CREATE DATABASE db_thuvien;
GO

USE db_thuvien;
GO

-- ===== TẠO LẠI TẤT CẢ CÁC BẢNG =====

-- bảng TAIKHOAN
CREATE TABLE TAIKHOAN (
	Email VARCHAR(50) PRIMARY KEY,
	[Password] VARCHAR(100) NOT NULL,
	HoTen NVARCHAR(100) NOT NULL,
	[Role] VARCHAR(100) NOT NULL CHECK ([Role] IN ('Admin', 'ThuThu')) DEFAULT 'ThuThu'
);
GO

-- bảng BANDOC
CREATE TABLE BANDOC (
	IdBD INT IDENTITY(1,1) PRIMARY KEY,
	HoTen NVARCHAR(100) NOT NULL,
	Email VARCHAR(100) UNIQUE,
	DiaChi NVARCHAR(255),
	SDT VARCHAR(15) UNIQUE
);
GO

-- Bảng NHAXUATBAN
CREATE TABLE NHAXUATBAN (
    MaNXB INT IDENTITY(1,1) PRIMARY KEY,
    TenNXB NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- Bảng THELOAI
CREATE TABLE THELOAI (
    MaTheLoai INT IDENTITY(1,1) PRIMARY KEY,
    TenTheLoai NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- Bảng TACGIA
CREATE TABLE TACGIA (
    MaTacGia INT IDENTITY(1,1) PRIMARY KEY,
    TenTacGia NVARCHAR(100) NOT NULL UNIQUE,
    Website VARCHAR(255),
    GhiChu NVARCHAR(MAX)
);
GO

-- bảng SACH
CREATE TABLE SACH (
	ISBN VARCHAR(20) PRIMARY KEY,
    TenSach NVARCHAR(255) NOT NULL,
	MaTacGia INT,
    MaTheLoai INT,
    NamXuatBan INT,
	DinhDang NVARCHAR(20) NOT NULL DEFAULT N'Bản in',
	MoTa NVARCHAR(MAX),
    MaNXB INT,
    GiaBia DECIMAL(10, 2),
    SoLuongTon INT DEFAULT 0 CHECK (SoLuongTon >= 0),
    SoTrang INT,

    CONSTRAINT FK_SACH_THELOAI FOREIGN KEY (MaTheLoai) REFERENCES THELOAI(MaTheLoai)
        ON UPDATE CASCADE ON DELETE NO ACTION,
    
    CONSTRAINT FK_SACH_NHAXUATBAN FOREIGN KEY (MaNXB) REFERENCES NHAXUATBAN(MaNXB)
        ON UPDATE CASCADE ON DELETE NO ACTION,
    
    CONSTRAINT FK_SACH_TACGIA FOREIGN KEY (MaTacGia) REFERENCES TACGIA(MaTacGia)
        ON UPDATE CASCADE 
        ON DELETE NO ACTION
);
GO

-- bảng BANSAO (bảng sao vật lý của sách)
CREATE TABLE BANSAO (
	MaBanSao INT IDENTITY(1,1) PRIMARY KEY,
	ISBN VARCHAR(20) NOT NULL,
	SoThuTuTrongKho INT NOT NULL,
	TinhTrang NVARCHAR(50) NOT NULL,
	NgayNhapKho DATE,
	ViTriLuuTru VARCHAR(50),

	FOREIGN KEY (ISBN) REFERENCES SACH(ISBN)
    ON DELETE NO ACTION ON UPDATE CASCADE,
	UNIQUE (ISBN, SoThuTuTrongKho)
);
GO

-- bảng PHIEUMUON
CREATE TABLE PHIEUMUON (
	IdPM INT IDENTITY(1,1) PRIMARY KEY,
	IdBD INT NOT NULL,
    EmailNguoiLap VARCHAR(50) NOT NULL,
	NgayMuon DATE NOT NULL DEFAULT GETDATE(),
	HanTra DATE NOT NULL,

	FOREIGN KEY (IdBD) REFERENCES BANDOC(IdBD)
	ON DELETE NO ACTION ON UPDATE CASCADE,
    FOREIGN KEY (EmailNguoiLap) REFERENCES TAIKHOAN(Email),
    CONSTRAINT CK_HanTra_Sau_NgayMuon CHECK (HanTra >= NgayMuon)
);
GO

-- Bảng CT_PM (Chi tiết Phiếu Mượn)
CREATE TABLE CT_PM (
    IdPM INT NOT NULL,
    MaBanSao INT NOT NULL,
    NgayTraThucTe DATE DEFAULT NULL,
    TinhTrangKhiTra NVARCHAR(50) DEFAULT NULL,
    EmailNguoiNhan VARCHAR(50) DEFAULT NULL,
    
    PRIMARY KEY (IdPM, MaBanSao),
    FOREIGN KEY (IdPM) REFERENCES PHIEUMUON(IdPM) ON DELETE CASCADE,
    FOREIGN KEY (MaBanSao) REFERENCES BANSAO(MaBanSao) ON DELETE NO ACTION,
    FOREIGN KEY (EmailNguoiNhan) REFERENCES TAIKHOAN(Email)
);
GO

-- Bảng PHAT
CREATE TABLE PHAT (
    IdPhat INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    IdPM INT NOT NULL,
    MaBanSao INT NOT NULL,
    LoaiPhat VARCHAR(20) NOT NULL CHECK (LoaiPhat IN ('Tre han', 'Hong sach', 'Mat sach')),
    SoTien DECIMAL(10, 0) NOT NULL,
    NgayGhiNhan DATE NOT NULL DEFAULT GETDATE(),
    TrangThai VARCHAR(20) NOT NULL CHECK (TrangThai IN ('Chua dong', 'Da dong')) DEFAULT 'Chua dong',
    
    FOREIGN KEY (IdPM, MaBanSao) REFERENCES CT_PM(IdPM, MaBanSao)
);
GO

-- ===== TẠO LẠI TRIGGERS =====

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
        RAISERROR (N'Sách có định dạng "Bản in" phải có Sô Trang lớn hơn 0.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
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
            RAISERROR (N'Khi có Ngày trả thực tế, Tình trạng khi trả không được phép NULL.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
    END
END;
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

PRINT '';
PRINT '========== ĐÃ RESET DATABASE THÀNH CÔNG ==========';
PRINT 'Database: db_thuvien';
PRINT 'Tất cả bảng đã được tái tạo';
PRINT 'Tất cả triggers đã được tái tạo';
PRINT 'Database đã sẵn sàng để thêm dữ liệu mẫu';
PRINT '===================================================';
GO
