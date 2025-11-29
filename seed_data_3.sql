use db_thuvien
GO
-- Lấy danh sách ISBN từ bảng SACH
DECLARE @ISBNList TABLE (RowNum INT IDENTITY(1,1), ISBN VARCHAR(20));

INSERT INTO @ISBNList(ISBN)
SELECT ISBN FROM SACH ORDER BY ISBN;   -- nếu ISBN NVARCHAR thì đổi cho đúng

DECLARE @totalISBN INT = (SELECT COUNT(*) FROM @ISBNList);
DECLARE @i INT = 1;        -- chỉ số ISBN
DECLARE @copyPerISBN INT = 7;    -- mỗi ISBN tạo 7 bản sao
DECLARE @bansao INT = 1;   -- bản sao tổng

WHILE @i <= @totalISBN
BEGIN
    DECLARE @isbn_code VARCHAR(20) =
        (SELECT ISBN FROM @ISBNList WHERE RowNum = @i);

    DECLARE @j INT = 1;

    WHILE @j <= @copyPerISBN
    BEGIN
        DECLARE @tinhTrang NVARCHAR(50) = CASE 
            WHEN @bansao % 10 = 0 THEN N'Cũ'
            WHEN @bansao % 10 = 1 THEN N'Rất Cũ'
            WHEN @bansao % 10 = 2 THEN N'Hỏng'
            ELSE N'Tốt'
        END;

        DECLARE @thuThuEmailBanSao VARCHAR(50) =
            'thuthu' + CAST(((@bansao - 1) % 9 + 1) AS NVARCHAR(2)) + '@thuvien.com';

        INSERT INTO BANSAO (ISBN, SoThuTuTrongKho, TinhTrang, NgayNhapKho, ViTriLuuTru, CreatedBy)
        VALUES (
            @isbn_code,
            @j, -- mỗi ISBN có 7 bản sao đánh số từ 1..7
            @tinhTrang,
            DATEADD(DAY, -(@bansao % 1000), CAST(GETDATE() AS DATE)),
            'KHO-' + RIGHT('000' + CAST(@bansao % 100 AS NVARCHAR(3)), 3),
            @thuThuEmailBanSao
        );

        SET @bansao = @bansao + 1;
        SET @j = @j + 1;
    END;

    SET @i = @i + 1;
END;
GO

-- THÊM NHIỀU PHIẾU MƯỢN
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

-- THÊM CHI TIẾT PHIẾU MƯỢN
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
            WHEN @ctpm % 20 = 1 THEN N'Rất Cũ'
            WHEN @ctpm % 20 = 2 THEN N'Cũ'
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

-- THÊM VÉ PHẠT
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