/*
============================================================
 SCRIPT THÊM DỮ LIỆU MẪU HÀNG NGÀN BẢN GHI (Đã chỉnh sửa)
============================================================
*/

USE db_thuvien;
GO

-- Xóa dữ liệu cũ theo thứ tự khóa ngoại ngược để tránh lỗi
DELETE FROM PHAT;
DELETE FROM CT_PM;
DELETE FROM PHIEUMUON;
DELETE FROM BANSAO;
DELETE FROM SACH;
DELETE FROM TACGIA;
DELETE FROM THELOAI;
DELETE FROM NHAXUATBAN;
DELETE FROM BANDOC;
DELETE FROM THONGBAO_NGUOINHAN;
DELETE FROM THONGBAO;
DELETE FROM YEUCAU_RESETMK;
DELETE FROM LICHLAM;
DELETE FROM TAIKHOAN;
GO

-- =============================================
-- 0. THÊM TÀI KHOẢN
-- =============================================
DECLARE @AdminEmail VARCHAR(50) = 'admin@thuvien.com';
DECLARE @DefaultPassword VARCHAR(100) = '123456'; -- Giả định đã được hash

INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role], CreatedBy) VALUES
(@AdminEmail, @DefaultPassword, N'Admin Thư Viện', 'Admin', NULL), -- Admin đầu tiên tự tạo
('thuthu1@thuvien.com', @DefaultPassword, N'Thủ Thư 1', 'ThuThu', @AdminEmail),
('thuthu2@thuvien.com', @DefaultPassword, N'Thủ Thư 2', 'ThuThu', @AdminEmail),
('thuthu3@thuvien.com', @DefaultPassword, N'Thủ Thư 3', 'ThuThu', @AdminEmail),
('thuthu4@thuvien.com', @DefaultPassword, N'Thủ Thư 4', 'ThuThu', @AdminEmail),
('thuthu5@thuvien.com', @DefaultPassword, N'Thủ Thư 5', 'ThuThu', @AdminEmail),
('thuthu6@thuvien.com', @DefaultPassword, N'Thủ Thư 6', 'ThuThu', @AdminEmail),
('thuthu7@thuvien.com', @DefaultPassword, N'Thủ Thư 7', 'ThuThu', @AdminEmail),
('thuthu8@thuvien.com', @DefaultPassword, N'Thủ Thư 8', 'ThuThu', @AdminEmail),
('thuthu9@thuvien.com', @DefaultPassword, N'Thủ Thư 9', 'ThuThu', @AdminEmail);
GO

DECLARE @AdminEmail VARCHAR(50) = 'admin@thuvien.com';

-- =============================================
-- 1. THÊM THỂ LOẠI
-- =============================================
SET IDENTITY_INSERT THELOAI ON;
INSERT INTO THELOAI (MaTheLoai, TenTheLoai) VALUES
(1, N'Kỹ năng sống'),
(2, N'Tiểu thuyết'),
(3, N'Văn học'),
(4, N'Sách thiếu nhi'),
(5, N'Khoa học');
SET IDENTITY_INSERT THELOAI OFF;
GO

-- =============================================
-- 2. THÊM NHIỀU TÁC GIẢ (100 BẢN GHI)
-- =============================================
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

-- =============================================
-- 3. THÊM NHIỀU NHÀ XUẤT BẢN (30 BẢN GHI)
-- =============================================
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

-- =============================================
-- 4. THÊM NHIỀU BẠN ĐỌC (500 BẢN GHI)
-- Đã thêm CreatedBy
-- =============================================
SET IDENTITY_INSERT BANDOC ON;
DECLARE @k INT = 1;
WHILE @k <= 500
BEGIN
    -- Phân chia CreatedBy luân phiên giữa 3 thủ thư
    DECLARE @thuThuEmailBD VARCHAR(50) = 'thuthu' + CAST(((@k % 3) + 1) AS NVARCHAR(2)) + '@thuvien.com';

    INSERT INTO BANDOC (IdBD, HoTen, Email, DiaChi, SDT, CreatedBy)
    VALUES (
        @k,
        N'Bạn Đọc ' + CAST(@k AS NVARCHAR(10)),
        'reader' + CAST(@k AS NVARCHAR(10)) + '@email.com',
        N'Địa chỉ số ' + CAST(@k AS NVARCHAR(10)) + N', Hồ Chí Minh',
        '09' + RIGHT('0000000000' + CAST(10000000 + @k AS NVARCHAR(10)), 8),
        @thuThuEmailBD
    );
    SET @k = @k + 1;
END;
SET IDENTITY_INSERT BANDOC OFF;
GO

-- =============================================
-- 5. THÊM NHIỀU SÁCH (300 BẢN GHI)
-- Đã thêm CreatedBy
-- =============================================
DECLARE @sach INT = 1;
WHILE @sach <= 300
BEGIN
    DECLARE @isbn VARCHAR(20) = '978-' + RIGHT('00000' + CAST(@sach AS NVARCHAR(10)), 5);
    DECLARE @tacGia INT = (@sach % 100) + 1;
    DECLARE @theLoai INT = ((@sach % 5) + 1);
    DECLARE @nxb INT = ((@sach % 30) + 1);
    DECLARE @nam INT = 2020 + (@sach % 5);
    DECLARE @giaBia DECIMAL(10, 2) = (50000 + (@sach * 1000)) % 300000;
    -- CreatedBy luân phiên giữa 2 thủ thư
    DECLARE @thuThuEmailSach VARCHAR(50) = 'thuthu' + CAST(((@sach % 2) + 1) AS NVARCHAR(2)) + '@thuvien.com';

    INSERT INTO SACH (ISBN, TenSach, MaTacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoLuongTon, SoTrang, CreatedBy)
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
        0, -- Sẽ được cập nhật bằng trigger sau khi thêm BANSAO
        200 + (@sach % 400),
        @thuThuEmailSach
    );
    SET @sach = @sach + 1;
END;
GO

-- =============================================
-- 6. THÊM NHIỀU BẢN SAO (2000 BẢN GHI)
-- Đã thêm CreatedBy
-- =============================================
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
    -- CreatedBy luân phiên giữa 3 thủ thư
    DECLARE @thuThuEmailBanSao VARCHAR(50) = 'thuthu' + CAST((((@bansao % 3) % 9) + 1) AS NVARCHAR(2)) + '@thuvien.com';
    
    INSERT INTO BANSAO (MaBanSao, ISBN, SoThuTuTrongKho, TinhTrang, NgayNhapKho, ViTriLuuTru, CreatedBy)
    VALUES (
        @bansao,
        @isbn_code,
        ((@bansao - 1) % 7) + 1, -- Đảm bảo SoThuTuTrongKho là duy nhất trong từng ISBN
        @tinhTrang,
        DATEADD(DAY, -(@bansao % 1000), GETDATE()),
        'KHO-' + CAST((@bansao % 100) AS NVARCHAR(3)),
        @thuThuEmailBanSao
    );
    
    IF @bansao % 7 = 0
        SET @isbn_idx = @isbn_idx + 1;
    
    SET @bansao = @bansao + 1;
END;
SET IDENTITY_INSERT BANSAO OFF;
GO

-- =============================================
-- 7. THÊM NHIỀU PHIẾU MƯỢN (1500 BẢN GHI)
-- =============================================
SET IDENTITY_INSERT PHIEUMUON ON;
DECLARE @pm INT = 1;
WHILE @pm <= 1500
BEGIN
    DECLARE @idBD INT = (@pm % 500) + 1;
    DECLARE @ngayMuon DATE = DATEADD(DAY, -(@pm % 60), GETDATE());
    DECLARE @hanTra DATE = DATEADD(DAY, 14, @ngayMuon);
    -- EmailNguoiLap luân phiên giữa 5 thủ thư
    DECLARE @emailLap VARCHAR(50) = 'thuthu' + CAST(((@pm % 5) + 1) AS NVARCHAR(2)) + '@thuvien.com';
    
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

-- =============================================
-- 8. THÊM CHI TIẾT PHIẾU MƯỢN (3000 BẢN GHI)
-- =============================================
DECLARE @ctpm INT = 1;
DECLARE @pm_idx INT = 1;
DECLARE @bansao_idx INT = 1;

-- Lấy tất cả MaBanSao và IdPM có thể kết hợp
DECLARE @MaxBanSao INT = (SELECT MAX(MaBanSao) FROM BANSAO);
DECLARE @MaxPM INT = (SELECT MAX(IdPM) FROM PHIEUMUON);

WHILE @ctpm <= 3000
BEGIN
    DECLARE @IdPM_CT INT = (@ctpm % @MaxPM) + 1;
    DECLARE @MaBanSao_CT INT = (@ctpm % @MaxBanSao) + 1;

    -- Kiểm tra nếu cặp (IdPM, MaBanSao) đã tồn tại thì bỏ qua
    IF EXISTS (SELECT 1 FROM CT_PM WHERE IdPM = @IdPM_CT AND MaBanSao = @MaBanSao_CT)
    BEGIN
        SET @ctpm = @ctpm + 1;
        CONTINUE;
    END

    DECLARE @ngayTraThucTe DATE = NULL;
    DECLARE @tinhTrangTra NVARCHAR(50) = NULL;
    DECLARE @emailNguoiNhan VARCHAR(50) = NULL;
    
    IF @ctpm % 5 < 3 -- Khoảng 60% đã trả
    BEGIN
        DECLARE @ngayMuon_CT DATE = (SELECT NgayMuon FROM PHIEUMUON WHERE IdPM = @IdPM_CT);
        -- Đảm bảo ngày trả lớn hơn ngày mượn
        SET @ngayTraThucTe = DATEADD(DAY, (@ctpm % 20), @ngayMuon_CT); 
        
        SET @tinhTrangTra = CASE 
            WHEN @ctpm % 20 = 0 THEN N'Hỏng'
            WHEN @ctpm % 20 = 1 THEN N'Cũ hơn'
            ELSE N'Tốt'
        END;
        -- EmailNguoiNhan luân phiên giữa 3 thủ thư
        SET @emailNguoiNhan = 'thuthu' + CAST(((@ctpm % 3) + 1) AS NVARCHAR(2)) + '@thuvien.com';
    END;
    
    INSERT INTO CT_PM (IdPM, MaBanSao, NgayTraThucTe, TinhTrangKhiTra, EmailNguoiNhan)
    VALUES (
        @IdPM_CT,
        @MaBanSao_CT,
        @ngayTraThucTe,
        @tinhTrangTra,
        @emailNguoiNhan
    );
    
    SET @ctpm = @ctpm + 1;
END;
GO

-- =============================================
-- 9. THÊM VÉ PHẠT (500 BẢN GHI)
-- =============================================
DELETE FROM PHAT;
GO

-- Tạo bảng temp để lấy (IdPM, MaBanSao) từ CT_PM (Chỉ lấy những bản ghi đã trả)
-- Tuy nhiên, phạt trễ hạn cũng có thể áp dụng cho bản ghi chưa trả
-- Chỉ lấy các cặp (IdPM, MaBanSao) *có* trong CT_PM
INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien, NgayGhiNhan, TrangThai)
SELECT TOP 500
    ct.IdPM,
    ct.MaBanSao,
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) % 3 = 0 THEN 'Tre han'
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) % 3 = 1 THEN 'Hong sach'
        ELSE 'Mat sach'
    END AS LoaiPhat,
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) % 3 = 0 THEN 5000 + (ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) * 100) % 50000
        WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) % 3 = 1 THEN 30000 + (ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) * 200) % 100000
        ELSE 50000 + (ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) * 300) % 200000
    END AS SoTien,
    DATEADD(DAY, -(ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) % 30), GETDATE()) AS NgayGhiNhan,
    CASE WHEN ROW_NUMBER() OVER (ORDER BY ct.IdPM, ct.MaBanSao) % 3 = 0 THEN 'Chua dong' ELSE 'Da dong' END AS TrangThai
FROM CT_PM ct
ORDER BY ct.IdPM, ct.MaBanSao;
GO

PRINT '';
PRINT '*** ĐÃ THÊM DỮ LIỆU MẪU HÀNG NGÀN BẢN GHI THÀNH CÔNG ***';
GO