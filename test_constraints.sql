/*
============================================================
 FILE 2: KIỂM TRA RÀNG BUỘC (TEST EXCEPTIONS)
============================================================
 Các lệnh dưới đây ĐƯỢC THIẾT KẾ ĐỂ GÂY LỖI
 Bạn có thể bôi đen từng khối và chạy để kiểm tra.
*/

USE db_thuvien;
GO

-- Test 1: CHECK (HanTra >= NgayMuon) trong PHIEUMUON
PRINT '--- Test 1: Hạn trả không thể trước ngày mượn ---';
BEGIN TRY
    INSERT INTO PHIEUMUON (IdBD, EmailNguoiLap, NgayMuon, HanTra) VALUES
    (101, 'thuthu1@thuvien.com', '2025-11-10', '2025-11-05'); -- Lỗi
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 2: TRIGGER (TRG_SACH_Insert_Update) - Sách in phải có SoTrang
PRINT '--- Test 2: Sách "Bản in" phải có SoTrang > 0 ---';
BEGIN TRY
    INSERT INTO SACH (ISBN, TenSach, MaTheLoai, MaNXB, DinhDang, SoTrang) VALUES
    ('TEST-ISBN-01', N'Sách Lỗi 1', 1, 1, N'Bản in', NULL); -- Lỗi
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 3: TRIGGER (TRG_SACH_Insert_Update) - SoTrang không thể bằng 0
PRINT '--- Test 3: Sách "Bản in" phải có SoTrang > 0 (Test case = 0) ---';
BEGIN TRY
    INSERT INTO SACH (ISBN, TenSach, MaTheLoai, MaNXB, DinhDang, SoTrang) VALUES
    ('TEST-ISBN-02', N'Sách Lỗi 2', 1, 1, N'Bản in', 0); -- Lỗi
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 4: TRIGGER (TRG_CT_PM_Update) - Trả sách phải ghi Tình trạng
PRINT '--- Test 4: Khi có NgayTraThucTe, TinhTrangKhiTra không được NULL ---';
BEGIN TRY
    -- Cập nhật một cuốn đang mượn (IdPM=1, MaBanSao=4)
    UPDATE CT_PM
    SET NgayTraThucTe = '2025-11-05',
        TinhTrangKhiTra = NULL -- Lỗi
    WHERE IdPM = 1 AND MaBanSao = 4;
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 5: CHECK [Role] trong TAIKHOAN
PRINT '--- Test 5: Role không hợp lệ ---';
BEGIN TRY
    INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role]) VALUES
    ('baduser@thuvien.com', '123', N'Người Lạ', 'NhanVien'); -- Lỗi
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 6: CHECK (LoaiPhat) trong PHAT
PRINT '--- Test 6: LoaiPhat không hợp lệ ---';
BEGIN TRY
    INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien) VALUES
    (1, 4, N'Làm bẩn sách', 5000); -- Lỗi
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 7: UNIQUE (Email) trong BANDOC
PRINT '--- Test 7: Trùng Email Bạn đọc ---';
BEGIN TRY
    INSERT INTO BANDOC (HoTen, Email, SDT) VALUES
    (N'Người Dùng Mới', 'duy@sv.com', '0999999999'); -- Lỗi (Email đã tồn tại)
END TRY
BEGIN CATCH
    PRINT 'LỖI: ' + ERROR_MESSAGE();
END CATCH
GO