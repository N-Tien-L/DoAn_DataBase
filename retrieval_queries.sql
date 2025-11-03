/*
============================================================
 FILE 3: TRUY VẤN DỮ LIỆU (RETRIEVAL QUERIES)
============================================================
*/

USE db_thuvien;
GO

-- 1. Xem tất cả tài khoản
SELECT * FROM TAIKHOAN;
GO

-- 2. Xem tất cả bạn đọc
SELECT * FROM BANDOC;
GO

-- 3. Xem tất cả đầu sách (dữ liệu thô)
SELECT * FROM SACH;
GO

-- 4. Xem thông tin Sách chi tiết (kèm Tên NXB, Tên Thể Loại)
SELECT 
    S.ISBN, 
    S.TenSach, 
    S.TacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoTrang
FROM 
    SACH AS S
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB;
GO

-- 5. Tìm các bản sao vật lý của một cuốn sách cụ thể
SELECT 
    S.TenSach,
    BS.MaBanSao,
    BS.SoThuTuTrongKho,
    BS.TinhTrang,
    BS.ViTriLuuTru
FROM 
    BANSAO AS BS
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
WHERE 
    S.TenSach LIKE N'%Đắc Nhân Tâm%';
GO

-- 6. Liệt kê các sách đang được mượn (chưa trả)
SELECT 
    BD.HoTen AS TenBanDoc,
    S.TenSach,
    PM.NgayMuon,
    PM.HanTra
FROM 
    CT_PM AS CTPM
JOIN 
    PHIEUMUON AS PM ON CTPM.IdPM = PM.IdPM
JOIN 
    BANDOC AS BD ON PM.IdBD = BD.IdBD
JOIN 
    BANSAO AS BS ON CTPM.MaBanSao = BS.MaBanSao
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
WHERE 
    CTPM.NgayTraThucTe IS NULL;
GO

-- 7. Thống kê tổng số sách mỗi bạn đọc đã mượn (tính cả đã trả)
SELECT 
    BD.HoTen, 
    COUNT(CTPM.MaBanSao) AS TongSoSachDaMuon
FROM 
    BANDOC AS BD
JOIN 
    PHIEUMUON AS PM ON BD.IdBD = PM.IdBD
JOIN 
    CT_PM AS CTPM ON PM.IdPM = CTPM.IdPM
GROUP BY 
    BD.IdBD, BD.HoTen
ORDER BY 
    TongSoSachDaMuon DESC;
GO

-- 8. Xem các khoản phạt chưa đóng
SELECT 
    P.IdPhat,
    BD.HoTen,
    S.TenSach,
    P.LoaiPhat,
    P.SoTien,
    P.NgayGhiNhan
FROM 
    PHAT AS P
JOIN 
    CT_PM AS CTPM ON P.IdPM = CTPM.IdPM AND P.MaBanSao = CTPM.MaBanSao
JOIN 
    PHIEUMUON AS PM ON CTPM.IdPM = PM.IdPM
JOIN 
    BANDOC AS BD ON PM.IdBD = BD.IdBD
JOIN 
    BANSAO AS BS ON CTPM.MaBanSao = BS.MaBanSao
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
WHERE 
    P.TrangThai = N'Chua dong';
GO

-- 9. Xem thông tin phiếu mượn (kèm tên Bạn đọc, tên Thủ thư lập phiếu)
SELECT 
    PM.IdPM,
    BD.HoTen AS TenBanDoc,
    TK.HoTen AS TenThuThuLapPhieu,
    PM.NgayMuon,
    PM.HanTra
FROM 
    PHIEUMUON AS PM
JOIN 
    BANDOC AS BD ON PM.IdBD = BD.IdBD
JOIN 
    TAIKHOAN AS TK ON PM.EmailNguoiLap = TK.Email;
GO

-- 10. Xem chi tiết các sách đã trả (kèm tên Thủ thư nhận sách)
SELECT 
    CTPM.IdPM,
    S.TenSach,
    CTPM.NgayTraThucTe,
    CTPM.TinhTrangKhiTra,
    TK.HoTen AS TenThuThuNhanSach
FROM 
    CT_PM AS CTPM
JOIN 
    BANSAO AS BS ON CTPM.MaBanSao = BS.MaBanSao
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
LEFT JOIN 
    TAIKHOAN AS TK ON CTPM.EmailNguoiNhan = TK.Email
WHERE 
    CTPM.NgayTraThucTe IS NOT NULL;
GO