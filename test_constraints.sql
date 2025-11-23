USE db_thuvien;
GO

-- Test 1: CHECK (HanTra >= NgayMuon) in PHIEUMUON
PRINT '--- Test 1: Due date cannot be earlier than borrow date ---';
BEGIN TRY
    INSERT INTO PHIEUMUON (IdBD, EmailNguoiLap, NgayMuon, HanTra) VALUES
    (101, 'thuthu1@thuvien.com', '2025-11-10', '2025-11-05'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 2: TRIGGER (TRG_SACH_Insert_Update) - Printed books must have SoTrang
PRINT '--- Test 2: "Printed" book must have SoTrang > 0 ---';
BEGIN TRY
    INSERT INTO SACH (ISBN, TenSach, MaTheLoai, MaNXB, DinhDang, SoTrang) VALUES
    ('TEST-ISBN-01', N'Error Book 1', 1, 1, N'Bản in', NULL); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 3: TRIGGER (TRG_SACH_Insert_Update) - SoTrang cannot be 0
PRINT '--- Test 3: "Printed" book must have SoTrang > 0 (Test case = 0) ---';
BEGIN TRY
    INSERT INTO SACH (ISBN, TenSach, MaTheLoai, MaNXB, DinhDang, SoTrang) VALUES
    ('TEST-ISBN-02', N'Error Book 2', 1, 1, N'Bản in', 0); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 4: TRIGGER (TRG_CT_PM_Update) - Return must include status
PRINT '--- Test 4: When NgayTraThucTe is set, TinhTrangKhiTra cannot be NULL ---';
BEGIN TRY
    -- Update a currently borrowed book (IdPM=1, MaBanSao=4)
    UPDATE CT_PM
    SET NgayTraThucTe = '2025-11-05',
        TinhTrangKhiTra = NULL -- Error
    WHERE IdPM = 1 AND MaBanSao = 4;
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 5: CHECK [Role] in TAIKHOAN
PRINT '--- Test 5: Invalid Role ---';
BEGIN TRY
    INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role]) VALUES
    ('baduser@thuvien.com', '123', N'Stranger', 'NhanVien'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 6: CHECK (LoaiPhat) in PHAT
PRINT '--- Test 6: Invalid LoaiPhat ---';
BEGIN TRY
    INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien, TrangThai) VALUES
    (1, 4, 'Dirty book', 5000, 'Chua dong'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 7: UNIQUE (Email) in BANDOC
PRINT '--- Test 7: Duplicate reader Email ---';
BEGIN TRY
    INSERT INTO BANDOC (HoTen, Email, SDT) VALUES
    (N'New User', 'duy@sv.com', '0999999999'); -- Error (Email already exists)
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 8: CHECK (TrangThai) in PHAT
PRINT '--- Test 8: Invalid fine TrangThai ---';
BEGIN TRY
    INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien, TrangThai) VALUES
    (1, 4, 'Tre han', 10000, 'Processing'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 9: TRIGGER (TRG_LICHLAM_NoOverlap) - Prevent overlapping shifts
PRINT '--- Test 9: Prevent overlapping work shifts ---';
BEGIN TRY
    -- Assume thuthu1 already has a shift from 08:00 to 12:00 on 2025-12-01
    INSERT INTO LICHLAM(EmailThuThu, Ngay, GioBatDau, GioKetThuc, CreatedBy) VALUES ('thuthu1@thuvien.com', '2025-12-01', '08:00:00', '12:00:00', 'admin@thuvien.com');
    -- Add an overlapping shift
    INSERT INTO LICHLAM(EmailThuThu, Ngay, GioBatDau, GioKetThuc, CreatedBy) VALUES ('thuthu1@thuvien.com', '2025-12-01', '11:00:00', '15:00:00', 'admin@thuvien.com'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 10: CHECK (Status) in TAIKHOAN
PRINT '--- Test 10: Invalid Status in TAIKHOAN ---';
BEGIN TRY
    INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role], Status) VALUES
    ('test@thuvien.com', '123', N'Test User', 'ThuThu', 'Pending'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 11: UNIQUE (SDT) in BANDOC
PRINT '--- Test 11: Duplicate phone number in BANDOC ---';
BEGIN TRY
    INSERT INTO BANDOC (HoTen, Email, SDT) VALUES
    (N'Another User', 'another@sv.com', '0901234567'); -- Error if this SDT exists
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 12: UNIQUE (TenNXB) in NHAXUATBAN
PRINT '--- Test 12: Duplicate publisher name ---';
BEGIN TRY
    INSERT INTO NHAXUATBAN (TenNXB) VALUES (N'Kim Đồng'); -- Error if exists
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 13: UNIQUE (TenTheLoai) in THELOAI
PRINT '--- Test 13: Duplicate genre name ---';
BEGIN TRY
    INSERT INTO THELOAI (TenTheLoai) VALUES (N'Văn học'); -- Error if exists
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 14: UNIQUE (TenTacGia) in TACGIA
PRINT '--- Test 14: Duplicate author name ---';
BEGIN TRY
    INSERT INTO TACGIA (TenTacGia) VALUES (N'Nguyễn Nhật Ánh'); -- Error if exists
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 15: UNIQUE (ISBN, SoThuTuTrongKho) in BANSAO
PRINT '--- Test 15: Duplicate copy number for same ISBN ---';
BEGIN TRY
    -- Assuming ISBN '978-604-2-12345-6' already has SoThuTuTrongKho = 1
    INSERT INTO BANSAO (ISBN, SoThuTuTrongKho, TinhTrang) VALUES
    ('978-604-2-12345-6', 1, N'Tốt'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 16: CHECK (SoLuongTon >= 0) in SACH
PRINT '--- Test 16: Negative SoLuongTon ---';
BEGIN TRY
    INSERT INTO SACH (ISBN, TenSach, MaTheLoai, MaNXB, SoLuongTon) VALUES
    ('TEST-NEG-01', N'Test Book', 1, 1, -5); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 17: CHECK (GioKetThuc > GioBatDau) in LICHLAM
PRINT '--- Test 17: End time before start time ---';
BEGIN TRY
    INSERT INTO LICHLAM(EmailThuThu, Ngay, GioBatDau, GioKetThuc, CreatedBy) VALUES
    ('thuthu1@thuvien.com', '2025-12-15', '14:00:00', '10:00:00', 'admin@thuvien.com'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

-- Test 18: CHECK (TrangThai) in LICHLAM
PRINT '--- Test 18: Invalid work shift status ---';
BEGIN TRY
    INSERT INTO LICHLAM(EmailThuThu, Ngay, GioBatDau, GioKetThuc, TrangThai, CreatedBy) VALUES
    ('thuthu1@thuvien.com', '2025-12-20', '08:00:00', '12:00:00', 'InProgress', 'admin@thuvien.com'); -- Error
END TRY
BEGIN CATCH
    PRINT 'ERROR: ' + ERROR_MESSAGE();
END CATCH
GO

PRINT N'===================================================';
PRINT N'ALL CONSTRAINT TESTS COMPLETED';
PRINT N'===================================================';
GO