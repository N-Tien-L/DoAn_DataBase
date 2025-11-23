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

-- 4. Xem thông tin Sách chi tiết (kèm Tên NXB, Tên Thể Loại, Tác Giả)
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoTrang,
	S.DinhDang,
	S.GiaBia
FROM 
    SACH AS S
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
LEFT JOIN
	TACGIA AS TG ON S.MaTacGia = TG.MaTacGia;
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
    P.TrangThai = 'Chua dong';
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

-- 11. Xem thống kê hồ sơ tài khoản thủ thư
SELECT * FROM VW_TAIKHOAN_ProfileStats;
GO

-- 12. Tìm sách theo tên (LIKE)
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoLuongTon,
    S.GiaBia
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    S.TenSach LIKE N'%Đắc Nhân Tâm%'; -- Thay đổi keyword tìm kiếm
GO

-- 13. Tìm sách theo tác giả
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    TG.TenTacGia LIKE N'%Nguyễn Nhật Ánh%'; -- Thay đổi tên tác giả
GO

-- 14. Tìm sách theo thể loại
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    T.TenTheLoai = N'Văn học'; -- Thay đổi thể loại
GO

-- 15. Tìm sách theo nhà xuất bản
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai,
    S.NamXuatBan,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    N.TenNXB LIKE N'%Kim Đồng%'; -- Thay đổi NXB
GO

-- 16. Tìm sách theo ISBN
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.DinhDang,
    S.SoTrang,
    S.GiaBia,
    S.SoLuongTon,
    S.MoTa
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    S.ISBN = '978-604-2-12345-6'; -- Thay đổi ISBN
GO

-- 17. Tìm sách theo năm xuất bản
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    S.NamXuatBan = 2020; -- Thay đổi năm
GO

-- 18. Tìm sách theo khoảng năm xuất bản
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    S.NamXuatBan BETWEEN 2015 AND 2025; -- Thay đổi khoảng năm
GO

-- 19. Tìm sách còn trong kho (SoLuongTon > 0)
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    S.SoLuongTon > 0
ORDER BY 
    S.TenSach;
GO

-- 20. Tìm sách theo định dạng (Bản in / Điện tử)
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    S.DinhDang,
    S.SoTrang,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
WHERE 
    S.DinhDang = N'Bản in'; -- Hoặc N'Điện tử'
GO

-- 21. Tìm kiếm sách đa điều kiện (tên, tác giả, thể loại)
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.NamXuatBan,
    S.SoLuongTon,
    S.GiaBia
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    (S.TenSach LIKE N'%keyword%' OR @TenSach IS NULL)
    AND (TG.TenTacGia LIKE N'%keyword%' OR @TacGia IS NULL)
    AND (T.MaTheLoai = @MaTheLoai OR @MaTheLoai IS NULL)
    AND (S.NamXuatBan = @NamXB OR @NamXB IS NULL);
GO

-- 22. Full-text search sách (tên, tác giả, mô tả)
SELECT 
    S.ISBN, 
    S.TenSach, 
    TG.TenTacGia, 
    T.TenTheLoai, 
    N.TenNXB,
    S.MoTa,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
LEFT JOIN 
    THELOAI AS T ON S.MaTheLoai = T.MaTheLoai
LEFT JOIN 
    NHAXUATBAN AS N ON S.MaNXB = N.MaNXB
WHERE 
    S.TenSach LIKE N'%keyword%'
    OR TG.TenTacGia LIKE N'%keyword%'
    OR S.MoTa LIKE N'%keyword%'
    OR N.TenNXB LIKE N'%keyword%';
GO

PRINT N'===================================================';
PRINT N'SEARCH QUERIES COMPLETED';
PRINT N'===================================================';
GO

-- 23. Xem tất cả lịch làm việc
SELECT 
    LL.Id,
    TK.HoTen AS TenThuThu,
    LL.Ngay,
    LL.GioBatDau,
    LL.GioKetThuc,
    LL.TrangThai,
    LL.GhiChu,
    TK_Creator.HoTen AS NguoiTao
FROM 
    LICHLAM AS LL
JOIN 
    TAIKHOAN AS TK ON LL.EmailThuThu = TK.Email
JOIN 
    TAIKHOAN AS TK_Creator ON LL.CreatedBy = TK_Creator.Email
ORDER BY 
    LL.Ngay DESC, LL.GioBatDau;
GO

-- 24. Xem lịch làm việc của một thủ thư cụ thể
SELECT 
    LL.Ngay,
    LL.GioBatDau,
    LL.GioKetThuc,
    LL.TrangThai,
    LL.GhiChu
FROM 
    LICHLAM AS LL
WHERE 
    LL.EmailThuThu = 'thuthu1@thuvien.com'
ORDER BY 
    LL.Ngay DESC, LL.GioBatDau;
GO

-- 25. Xem tất cả thông báo
SELECT 
    TB.Id,
    TB.TieuDe,
    TB.NoiDung,
    TK.HoTen AS NguoiTao,
    TB.CreatedAt
FROM 
    THONGBAO AS TB
JOIN 
    TAIKHOAN AS TK ON TB.CreatedBy = TK.Email
ORDER BY 
    TB.CreatedAt DESC;
GO

-- 26. Xem thông báo của một người dùng cụ thể (kèm trạng thái đã đọc)
SELECT 
    TB.TieuDe,
    TB.NoiDung,
    TB.CreatedAt,
    TBNN.DaDoc,
    TBNN.ReadAt
FROM 
    THONGBAO_NGUOINHAN AS TBNN
JOIN 
    THONGBAO AS TB ON TBNN.IdThongBao = TB.Id
WHERE 
    TBNN.EmailNhan = 'thuthu1@thuvien.com'
ORDER BY 
    TB.CreatedAt DESC;
GO

-- 27. Xem tất cả yêu cầu reset mật khẩu
SELECT 
    YC.Id,
    TK.HoTen AS TenThuThu,
    YC.LyDo,
    YC.TrangThai,
    YC.CreatedAt,
    TK_XuLy.HoTen AS NguoiXuLy,
    YC.XuLyLuc,
    YC.GhiChuXuLy
FROM 
    YEUCAU_RESETMK AS YC
JOIN 
    TAIKHOAN AS TK ON YC.EmailThuThu = TK.Email
LEFT JOIN 
    TAIKHOAN AS TK_XuLy ON YC.XuLyBoi = TK_XuLy.Email
ORDER BY 
    YC.CreatedAt DESC;
GO

-- 28. Xem yêu cầu reset mật khẩu đang chờ xử lý
SELECT 
    YC.Id,
    TK.HoTen AS TenThuThu,
    TK.Email,
    YC.LyDo,
    YC.CreatedAt
FROM 
    YEUCAU_RESETMK AS YC
JOIN 
    TAIKHOAN AS TK ON YC.EmailThuThu = TK.Email
WHERE 
    YC.TrangThai = 'Pending'
ORDER BY 
    YC.CreatedAt;
GO

-- 29. Thống kê sách theo thể loại
SELECT 
    T.TenTheLoai,
    COUNT(S.ISBN) AS SoLuongDauSach,
    SUM(S.SoLuongTon) AS TongSoBanSao
FROM 
    THELOAI AS T
LEFT JOIN 
    SACH AS S ON T.MaTheLoai = S.MaTheLoai
GROUP BY 
    T.MaTheLoai, T.TenTheLoai
ORDER BY 
    SoLuongDauSach DESC;
GO

-- 30. Thống kê sách theo nhà xuất bản
SELECT 
    N.TenNXB,
    COUNT(S.ISBN) AS SoLuongDauSach,
    SUM(S.SoLuongTon) AS TongSoBanSao
FROM 
    NHAXUATBAN AS N
LEFT JOIN 
    SACH AS S ON N.MaNXB = S.MaNXB
GROUP BY 
    N.MaNXB, N.TenNXB
ORDER BY 
    SoLuongDauSach DESC;
GO

-- 31. Thống kê sách theo tác giả
SELECT 
    TG.TenTacGia,
    COUNT(S.ISBN) AS SoLuongDauSach,
    SUM(S.SoLuongTon) AS TongSoBanSao
FROM 
    TACGIA AS TG
LEFT JOIN 
    SACH AS S ON TG.MaTacGia = S.MaTacGia
GROUP BY 
    TG.MaTacGia, TG.TenTacGia
ORDER BY 
    SoLuongDauSach DESC;
GO

-- 32. Xem sách sắp hết (SoLuongTon < 5)
SELECT 
    S.ISBN,
    S.TenSach,
    TG.TenTacGia,
    S.SoLuongTon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
WHERE 
    S.SoLuongTon < 5
ORDER BY 
    S.SoLuongTon;
GO

-- 33. Xem sách quá hạn trả (chưa trả và đã quá HanTra)
SELECT 
    BD.HoTen AS TenBanDoc,
    BD.Email,
    BD.SDT,
    S.TenSach,
    PM.NgayMuon,
    PM.HanTra,
    DATEDIFF(DAY, PM.HanTra, CAST(SYSUTCDATETIME() AS DATE)) AS SoNgayQuaHan
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
    CTPM.NgayTraThucTe IS NULL
    AND PM.HanTra < CAST(SYSUTCDATETIME() AS DATE)
ORDER BY 
    SoNgayQuaHan DESC;
GO

-- 34. Xem top 10 bạn đọc mượn nhiều nhất
SELECT TOP 10
    BD.IdBD,
    BD.HoTen,
    BD.Email,
    COUNT(CTPM.MaBanSao) AS TongSoLanMuon
FROM 
    BANDOC AS BD
JOIN 
    PHIEUMUON AS PM ON BD.IdBD = PM.IdBD
JOIN 
    CT_PM AS CTPM ON PM.IdPM = CTPM.IdPM
GROUP BY 
    BD.IdBD, BD.HoTen, BD.Email
ORDER BY 
    TongSoLanMuon DESC;
GO

-- 35. Xem top 10 sách được mượn nhiều nhất
SELECT TOP 10
    S.ISBN,
    S.TenSach,
    TG.TenTacGia,
    COUNT(CTPM.MaBanSao) AS SoLanMuon
FROM 
    SACH AS S
LEFT JOIN 
    TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
JOIN 
    BANSAO AS BS ON S.ISBN = BS.ISBN
JOIN 
    CT_PM AS CTPM ON BS.MaBanSao = CTPM.MaBanSao
GROUP BY 
    S.ISBN, S.TenSach, TG.TenTacGia
ORDER BY 
    SoLanMuon DESC;
GO

-- 36. Thống kê tổng thu từ phạt (theo loại phạt)
SELECT 
    P.LoaiPhat,
    COUNT(*) AS SoLuongPhat,
    SUM(CASE WHEN P.TrangThai = 'Da dong' THEN P.SoTien ELSE 0 END) AS TongTienDaDong,
    SUM(CASE WHEN P.TrangThai = 'Chua dong' THEN P.SoTien ELSE 0 END) AS TongTienChuaDong,
    SUM(P.SoTien) AS TongTien
FROM 
    PHAT AS P
GROUP BY 
    P.LoaiPhat;
GO

-- 37. Xem lịch sử mượn trả của một bạn đọc cụ thể
SELECT 
    PM.IdPM,
    S.TenSach,
    PM.NgayMuon,
    PM.HanTra,
    CTPM.NgayTraThucTe,
    CTPM.TinhTrangKhiTra,
    CASE 
        WHEN CTPM.NgayTraThucTe IS NULL THEN N'Đang mượn'
        WHEN CTPM.NgayTraThucTe > PM.HanTra THEN N'Trả trễ'
        ELSE N'Trả đúng hạn'
    END AS TrangThaiTra
FROM 
    PHIEUMUON AS PM
JOIN 
    CT_PM AS CTPM ON PM.IdPM = CTPM.IdPM
JOIN 
    BANSAO AS BS ON CTPM.MaBanSao = BS.MaBanSao
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
WHERE 
    PM.IdBD = 101 -- Thay đổi IdBD theo nhu cầu
ORDER BY 
    PM.NgayMuon DESC;
GO

-- 38. Xem bản sao đang được mượn (trạng thái sách)
SELECT 
    S.TenSach,
    BS.MaBanSao,
    BS.SoThuTuTrongKho,
    BD.HoTen AS TenBanDoc,
    PM.NgayMuon,
    PM.HanTra
FROM 
    BANSAO AS BS
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
JOIN 
    CT_PM AS CTPM ON BS.MaBanSao = CTPM.MaBanSao
JOIN 
    PHIEUMUON AS PM ON CTPM.IdPM = PM.IdPM
JOIN 
    BANDOC AS BD ON PM.IdBD = BD.IdBD
WHERE 
    CTPM.NgayTraThucTe IS NULL;
GO

-- 39. Xem bản sao còn trong kho (chưa được mượn)
SELECT 
    S.TenSach,
    BS.MaBanSao,
    BS.SoThuTuTrongKho,
    BS.TinhTrang,
    BS.ViTriLuuTru,
    BS.NgayNhapKho
FROM 
    BANSAO AS BS
JOIN 
    SACH AS S ON BS.ISBN = S.ISBN
WHERE 
    BS.MaBanSao NOT IN (
        SELECT MaBanSao 
        FROM CT_PM 
        WHERE NgayTraThucTe IS NULL
    )
ORDER BY 
    S.TenSach, BS.SoThuTuTrongKho;
GO

-- 40. Thống kê hoạt động theo tháng (số phiếu mượn)
SELECT 
    YEAR(PM.NgayMuon) AS Nam,
    MONTH(PM.NgayMuon) AS Thang,
    COUNT(PM.IdPM) AS SoPhieuMuon,
    COUNT(CTPM.MaBanSao) AS TongSoSachMuon
FROM 
    PHIEUMUON AS PM
JOIN 
    CT_PM AS CTPM ON PM.IdPM = CTPM.IdPM
GROUP BY 
    YEAR(PM.NgayMuon), MONTH(PM.NgayMuon)
ORDER BY 
    Nam DESC, Thang DESC;
GO

-- 41. Xem thống kê tổng quan hệ thống
SELECT 
    (SELECT COUNT(*) FROM TAIKHOAN) AS TongTaiKhoan,
    (SELECT COUNT(*) FROM BANDOC) AS TongBanDoc,
    (SELECT COUNT(*) FROM SACH) AS TongDauSach,
    (SELECT SUM(SoLuongTon) FROM SACH) AS TongBanSao,
    (SELECT COUNT(*) FROM PHIEUMUON) AS TongPhieuMuon,
    (SELECT COUNT(*) FROM CT_PM WHERE NgayTraThucTe IS NULL) AS SachDangMuon,
    (SELECT COUNT(*) FROM PHAT WHERE TrangThai = 'Chua dong') AS PhatChuaDong,
    (SELECT SUM(SoTien) FROM PHAT WHERE TrangThai = 'Chua dong') AS TongTienPhatChuaDong;
GO

PRINT N'===================================================';
PRINT N'ALL RETRIEVAL QUERIES COMPLETED';
PRINT N'===================================================';
GO