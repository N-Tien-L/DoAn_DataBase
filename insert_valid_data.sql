/*
============================================================
 FILE 1: INSERT DỮ LIỆU HỢP LỆ (VALID DATA)
============================================================
*/

USE db_thuvien;
GO

-- Xóa dữ liệu cũ theo đúng thứ tự (từ bảng con đến bảng cha) để tránh lỗi khóa ngoại
DELETE FROM PHAT;
DELETE FROM CT_PM;
DELETE FROM PHIEUMUON;
DELETE FROM BANSAO;
DELETE FROM SACH;
DELETE FROM THELOAI;
DELETE FROM NHAXUATBAN;
DELETE FROM BANDOC;
DELETE FROM TAIKHOAN;
GO

-- 1. bảng TAIKHOAN
INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role]) VALUES
('admin@thuvien.com', 'hashed_pass_1', N'Nguyễn Văn Admin', 'Admin'),
('thuthu1@thuvien.com', 'hashed_pass_2', N'Trần Thị Thủ Thư', 'ThuThu'),
('thuthu2@thuvien.com', 'hashed_pass_3', N'Lê Văn C', 'ThuThu');
GO

-- 2. bảng BANDOC
SET IDENTITY_INSERT BANDOC ON;
INSERT INTO BANDOC (IdBD, HoTen, Email, DiaChi, SDT) VALUES
(101, N'Phạm Minh Duy', 'duy@sv.com', N'203 CMT8, Q.10, HCM', '0901234567'),
(102, N'Vũ Kim Anh', 'anh@sv.com', N'1A Lê Lợi, Q.1, HCM', '0918765432'),
(103, N'Hoàng Trung', 'trung@sv.com', N'Ký túc xá B, Thủ Đức', '0987654321');
SET IDENTITY_INSERT BANDOC OFF;
GO

-- 3. Bảng NHAXUATBAN
SET IDENTITY_INSERT NHAXUATBAN ON;
INSERT INTO NHAXUATBAN (MaNXB, TenNXB) VALUES
(1, N'NXB Trẻ'),
(2, N'NXB Giáo Dục'),
(3, N'NXB Kim Đồng'),
(4, N'NXB Văn Học');
SET IDENTITY_INSERT NHAXUATBAN OFF;
GO

-- 4. Bảng THELOAI
SET IDENTITY_INSERT THELOAI ON;
INSERT INTO THELOAI (MaTheLoai, TenTheLoai) VALUES
(1, N'Kỹ năng sống'),
(2, N'Tiểu thuyết'),
(3, N'Văn học'),
(4, N'Sách thiếu nhi'),
(5, N'Khoa học');
SET IDENTITY_INSERT THELOAI OFF;
GO

-- 5. bảng SACH
INSERT INTO SACH (ISBN, TenSach, TacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoLuongTon, SoTrang) VALUES
('978-604-58-1541-0', N'Đắc Nhân Tâm', N'Dale Carnegie', 1, 2018, N'Bản in', N'Sách về nghệ thuật ứng xử...', 1, 120000.00, 3, 350),
('978-604-68-1234-5', N'Nhà Giả Kim', N'Paulo Coelho', 2, 2015, N'Bản in', N'Một tiểu thuyết phiêu lưu...', 4, 85000.00, 2, 250),
('978-006-112008-4', N'To Kill a Mockingbird (eBook)', N'Harper Lee', 2, 2006, N'eBook', N'Tiểu thuyết kinh điển của Mỹ...', 1, 0.00, 0, NULL),
('978-604-2-06915-0', N'Dế Mèn Phiêu Lưu Ký', N'Tô Hoài', 4, 2010, N'Bản in', N'Truyện thiếu nhi nổi tiếng...', 3, 50000.00, 5, 150);
GO

-- 6. bảng BANSAO
SET IDENTITY_INSERT BANSAO ON;
INSERT INTO BANSAO (MaBanSao, ISBN, SoThuTuTrongKho, TinhTrang, NgayNhapKho, ViTriLuuTru) VALUES
(1, '978-604-58-1541-0', 1, N'Tốt', '2023-01-10', 'KNS-A1'), -- Đắc Nhân Tâm 1
(2, '978-604-58-1541-0', 2, N'Cũ', '2023-01-10', 'KNS-A1'), -- Đắc Nhân Tâm 2
(3, '978-604-58-1541-0', 3, N'Tốt', '2024-05-20', 'KNS-A1'), -- Đắc Nhân Tâm 3
(4, '978-604-68-1234-5', 1, N'Tốt', '2023-02-01', 'TN-B3'), -- Nhà Giả Kim 1
(5, '978-604-68-1234-5', 2, N'Tốt', '2023-02-01', 'TN-B3'), -- Nhà Giả Kim 2
(6, '978-604-2-06915-0', 1, N'Mới', '2023-03-03', 'TTN-C1'); -- Dế Mèn 1
SET IDENTITY_INSERT BANSAO OFF;
GO

-- 7. bảng PHIEUMUON
SET IDENTITY_INSERT PHIEUMUON ON;
INSERT INTO PHIEUMUON (IdPM, IdBD, EmailNguoiLap, NgayMuon, HanTra) VALUES
(1, 101, 'thuthu1@thuvien.com', '2025-10-20', '2025-11-03'), -- Duy mượn
(2, 102, 'thuthu2@thuvien.com', '2025-10-25', '2025-11-08'); -- Kim Anh mượn
SET IDENTITY_INSERT PHIEUMUON OFF;
GO

-- 8. Bảng CT_PM
INSERT INTO CT_PM (IdPM, MaBanSao, NgayTraThucTe, TinhTrangKhiTra, EmailNguoiNhan) VALUES
-- Phiếu 1 (Duy):
(1, 1, '2025-11-01', N'Tốt', 'thuthu1@thuvien.com'),  -- Đắc Nhân Tâm 1 (Đã trả)
(1, 4, NULL, NULL, NULL),                            -- Nhà Giả Kim 1 (Đang mượn)

-- Phiếu 2 (Kim Anh):
(2, 2, '2025-11-01', N'Cũ hơn', 'thuthu2@thuvien.com'), -- Đắc Nhân Tâm 2 (Đã trả)
(2, 6, NULL, NULL, NULL);                           -- Dế Mèn 1 (Đang mượn)
GO

-- 9. Bảng PHAT
INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien, NgayGhiNhan, TrangThai) VALUES
-- Giả sử cuốn Nhà Giả Kim (IdPM=1, MaBanSao=4) bị hỏng khi đang mượn
(1, 4, 'Hong sach', 50000.00, '2025-10-30', 'Chua dong'),
-- Giả sử cuốn Đắc Nhân Tâm (IdPM=2, MaBanSao=2) bị trả trễ hạn
(2, 2, 'Tre han', 15000.00, '2025-11-01', 'Da dong');
GO

PRINT '*** ĐÃ CHÈN DỮ LIỆU HỢP LỆ THÀNH CÔNG ***';
GO