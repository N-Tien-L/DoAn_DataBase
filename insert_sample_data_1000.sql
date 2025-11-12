/*
============================================================
 SCRIPT THÊM DỮ LIỆU MẪU HÀNG NGÀN BẢN GHI
============================================================
*/

USE db_thuvien;
GO

-- ===== 0. THÊM TÀI KHOẢN =====
DELETE FROM TAIKHOAN;
GO

INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role]) VALUES
('admin@thuvien.com', '123456', N'Admin Thư Viện', 'Admin'),
('thuthu1@thuvien.com', 'hashed_pass_2', N'Thủ Thư 1', 'ThuThu'),
('thuthu2@thuvien.com', 'hashed_pass_3', N'Thủ Thư 2', 'ThuThu'),
('thuthu3@thuvien.com', 'hashed_pass_4', N'Thủ Thư 3', 'ThuThu'),
('thuthu4@thuvien.com', 'hashed_pass_5', N'Thủ Thư 4', 'ThuThu'),
('thuthu5@thuvien.com', 'hashed_pass_6', N'Thủ Thư 5', 'ThuThu'),
('thuthu6@thuvien.com', 'hashed_pass_7', N'Thủ Thư 6', 'ThuThu'),
('thuthu7@thuvien.com', 'hashed_pass_8', N'Thủ Thư 7', 'ThuThu'),
('thuthu8@thuvien.com', 'hashed_pass_9', N'Thủ Thư 8', 'ThuThu'),
('thuthu9@thuvien.com', 'hashed_pass_10', N'Thủ Thư 9', 'ThuThu');
GO

-- ===== 1. THÊM THỂ LOẠI =====
DELETE FROM THELOAI;
GO

SET IDENTITY_INSERT THELOAI ON;
INSERT INTO THELOAI (MaTheLoai, TenTheLoai) VALUES
(1, N'Kỹ năng sống'),
(2, N'Tiểu thuyết'),
(3, N'Văn học'),
(4, N'Sách thiếu nhi'),
(5, N'Khoa học');
SET IDENTITY_INSERT THELOAI OFF;
GO

-- ===== 2. THÊM NHIỀU TÀC GIÃ (100 BẢN GHI) =====
DELETE FROM TACGIA;
GO

SET IDENTITY_INSERT TACGIA ON;
DECLARE @i INT = 1;
WHILE @i <= 100
BEGIN
    INSERT INTO TACGIA (MaTacGia, TenTacGia, Website, GhiChu)
    VALUES (
        @i,
        N'Tác Giả ' + CAST(@i AS NVARCHAR(10)),
        'https://author' + CAST(@i AS NVARCHAR(10)) + '.com',
        N'Tác giả số ' + CAST(@i AS NVARCHAR(10))
    );
    SET @i = @i + 1;
END;
SET IDENTITY_INSERT TACGIA OFF;
GO

-- ===== 3. THÊM NHIỀU NHÀ XUẤT BẢN (30 BẢN GHI) =====
DELETE FROM NHAXUATBAN;
GO

SET IDENTITY_INSERT NHAXUATBAN ON;
DECLARE @j INT = 1;
WHILE @j <= 30
BEGIN
    INSERT INTO NHAXUATBAN (MaNXB, TenNXB)
    VALUES (@j, N'Nhà Xuất Bản ' + CAST(@j AS NVARCHAR(10)));
    SET @j = @j + 1;
END;
SET IDENTITY_INSERT NHAXUATBAN OFF;
GO

-- ===== 4. THÊM NHIỀU BẠN ĐỌC (500 BẢN GHI) =====
DELETE FROM BANDOC;
GO

SET IDENTITY_INSERT BANDOC ON;
DECLARE @k INT = 1;
WHILE @k <= 500
BEGIN
    INSERT INTO BANDOC (IdBD, HoTen, Email, DiaChi, SDT)
    VALUES (
        @k,
        N'Bạn Đọc ' + CAST(@k AS NVARCHAR(10)),
        'reader' + CAST(@k AS NVARCHAR(10)) + '@email.com',
        N'Địa chỉ số ' + CAST(@k AS NVARCHAR(10)) + N', Hồ Chí Minh',
        '09' + RIGHT('0000000000' + CAST(10000000 + @k AS NVARCHAR(10)), 8)
    );
    SET @k = @k + 1;
END;
SET IDENTITY_INSERT BANDOC OFF;
GO

-- ===== 5. THÊM NHIỀU SÁCH (300 BẢN GHI) =====
DELETE FROM SACH;
GO

DECLARE @sach INT = 1;
WHILE @sach <= 300
BEGIN
    DECLARE @isbn VARCHAR(20) = '978-' + RIGHT('00000' + CAST(@sach AS NVARCHAR(10)), 5);
    DECLARE @tacGia INT = (@sach % 100) + 1;
    DECLARE @theLoai INT = ((@sach % 5) + 1);
    DECLARE @nxb INT = ((@sach % 30) + 1);
    DECLARE @nam INT = 2020 + (@sach % 5);
    DECLARE @giaBia DECIMAL(10, 2) = (50000 + (@sach * 1000)) % 300000;
    
    INSERT INTO SACH (ISBN, TenSach, MaTacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoLuongTon, SoTrang)
    VALUES (
        @isbn,
        N'Sách Số ' + CAST(@sach AS NVARCHAR(10)),
        @tacGia,
        @theLoai,
        @nam,
        N'Bản in',
        N'Mô tả sách số ' + CAST(@sach AS NVARCHAR(10)),
        @nxb,
        @giaBia,
        0,
        200 + (@sach % 400)
    );
    SET @sach = @sach + 1;
END;
GO

-- ===== 6. THÊM NHIỀU BẢN SÁO (2000 BẢN GHI) =====
DELETE FROM BANSAO;
GO

SET IDENTITY_INSERT BANSAO ON;
DECLARE @bansao INT = 1;
DECLARE @isbn_idx INT = 1;
WHILE @bansao <= 2000
BEGIN
    DECLARE @isbn_code VARCHAR(20) = '978-' + RIGHT('00000' + CAST(@isbn_idx AS NVARCHAR(10)), 5);
    DECLARE @tinhTrang NVARCHAR(50) = CASE 
        WHEN @bansao % 10 = 0 THEN N'Cũ'
        WHEN @bansao % 10 = 1 THEN N'Rất cũ'
        ELSE N'Tốt'
    END;
    
    INSERT INTO BANSAO (MaBanSao, ISBN, SoThuTuTrongKho, TinhTrang, NgayNhapKho, ViTriLuuTru)
    VALUES (
        @bansao,
        @isbn_code,
        (@bansao % 10) + 1,
        @tinhTrang,
        DATEADD(DAY, -(@bansao % 1000), GETDATE()),
        'KHO-' + CAST((@bansao % 100) AS NVARCHAR(3))
    );
    
    IF @bansao % 7 = 0
        SET @isbn_idx = @isbn_idx + 1;
    
    SET @bansao = @bansao + 1;
END;
SET IDENTITY_INSERT BANSAO OFF;
GO

-- ===== 7. THÊM NHIỀU PHIẾU MƯỢN (1500 BẢN GHI) =====
DELETE FROM CT_PM;
DELETE FROM PHIEUMUON;
GO

SET IDENTITY_INSERT PHIEUMUON ON;
DECLARE @pm INT = 1;
WHILE @pm <= 1500
BEGIN
    DECLARE @idBD INT = (@pm % 500) + 1;
    DECLARE @ngayMuon DATE = DATEADD(DAY, -(@pm % 60), GETDATE());
    DECLARE @hanTra DATE = DATEADD(DAY, 14, @ngayMuon);
    DECLARE @emailLap VARCHAR(50) = 'thuthu' + CAST(((@pm % 9) + 1) AS NVARCHAR(2)) + '@thuvien.com';
    
    INSERT INTO PHIEUMUON (IdPM, IdBD, EmailNguoiLap, NgayMuon, HanTra)
    VALUES (
        @pm,
        @idBD,
        @emailLap,
        @ngayMuon,
        @hanTra
    );
    SET @pm = @pm + 1;
END;
SET IDENTITY_INSERT PHIEUMUON OFF;
GO

-- ===== 8. THÊM CHI TIẾT PHIẾU MƯỢN (3000 BẢN GHI) =====
DECLARE @ctpm INT = 1;
DECLARE @pm_idx INT = 1;
DECLARE @bansao_idx INT = 1;

WHILE @ctpm <= 3000
BEGIN
    DECLARE @ngayTraThucTe DATE = NULL;
    DECLARE @tinhTrangTra NVARCHAR(50) = NULL;
    DECLARE @emailNguoiNhan VARCHAR(50) = NULL;
    
    IF @ctpm % 5 < 3
    BEGIN
        SET @ngayTraThucTe = DATEADD(DAY, -(@ctpm % 40), GETDATE());
        SET @tinhTrangTra = CASE 
            WHEN @ctpm % 20 = 0 THEN N'Hỏng'
            WHEN @ctpm % 20 = 1 THEN N'Cũ hơn'
            ELSE N'Tốt'
        END;
        SET @emailNguoiNhan = 'thuthu' + CAST(((@ctpm % 9) + 1) AS NVARCHAR(2)) + '@thuvien.com';
    END;
    
    BEGIN TRY
        INSERT INTO CT_PM (IdPM, MaBanSao, NgayTraThucTe, TinhTrangKhiTra, EmailNguoiNhan)
        VALUES (
            (@pm_idx % 1500) + 1,
            (@bansao_idx % 2000) + 1,
            @ngayTraThucTe,
            @tinhTrangTra,
            @emailNguoiNhan
        );
    END TRY
    BEGIN CATCH
    END CATCH;
    
    SET @bansao_idx = @bansao_idx + 1;
    IF @bansao_idx % 2 = 0
        SET @pm_idx = @pm_idx + 1;
    
    SET @ctpm = @ctpm + 1;
END;
GO

-- ===== 9. THÊM VÉ PHẠT (500 BẢN GHI) =====
DELETE FROM PHAT;
GO

-- Tạo bảng temp để lấy (IdPM, MaBanSao) từ CT_PM
DECLARE @phat INT = 1;

-- Lấy dữ liệu từ CT_PM để tạo PHAT
INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien, NgayGhiNhan, TrangThai)
SELECT TOP 500
    ct.IdPM,
    ct.MaBanSao,
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM) % 3 = 0 THEN 'Tre han'
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM) % 3 = 1 THEN 'Hong sach'
        ELSE 'Mat sach'
    END AS LoaiPhat,
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM) % 3 = 0 THEN 5000 + (ROW_NUMBER() OVER (ORDER BY ct.IdPM) * 100) % 50000
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM) % 3 = 1 THEN 30000 + (ROW_NUMBER() OVER (ORDER BY ct.IdPM) * 200) % 100000
        ELSE 50000 + (ROW_NUMBER() OVER (ORDER BY ct.IdPM) * 300) % 200000
    END AS SoTien,
    DATEADD(DAY, -(ROW_NUMBER() OVER (ORDER BY ct.IdPM) % 30), GETDATE()) AS NgayGhiNhan,
    CASE WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM) % 3 = 0 THEN 'Chua dong' ELSE 'Da dong' END AS TrangThai
FROM CT_PM ct
ORDER BY ct.IdPM;
GO

PRINT '';
PRINT '*** ĐÃ THÊM DỮ LIỆU MẪU HÀNG NGÀN BẢN GHI THÀNH CÔNG ***';
GO
