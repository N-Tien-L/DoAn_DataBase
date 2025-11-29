use db_thuvien
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

-- thêm TÀI KHOẢN
DECLARE @AdminEmail VARCHAR(50) = 'admin@thuvien.com';
DECLARE @DefaultPassword VARCHAR(100) = '123456';

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

-- thêm THỂ LOẠI
SET IDENTITY_INSERT THELOAI ON;
INSERT INTO THELOAI (MaTheLoai, TenTheLoai) VALUES
(1, N'Kỹ năng sống'),
(2, N'Tiểu thuyết'),
(3, N'Văn học'),
(4, N'Sách thiếu nhi'),
(5, N'Khoa học'),
(6, N'Kinh tế'),
(7, N'Lập trình'),
(8, N'Công nghệ thông tin'),
(9, N'Tâm lý học'),
(10, N'Triết học'),
(11, N'Lịch sử'),
(12, N'Địa lý'),
(13, N'Tiếng Anh'),
(14, N'Tiểu sử - Hồi ký'),
(15, N'Y học'),
(16, N'Khởi nghiệp')
SET IDENTITY_INSERT THELOAI OFF;
GO

-- thêm TÁC GIẢ
INSERT INTO TACGIA (TenTacGia, Website, GhiChu) VALUES
(N'Nguyễn Nhật Ánh', 'https://nguyennhatanh.vn', N'Nhà văn thiếu nhi'),
(N'Haruki Murakami', 'https://murakami.jp', N'Nhà văn Nhật Bản'),
(N'J.K. Rowling', 'https://www.jkrowling.com', N'Tác giả Harry Potter'),
(N'Paulo Coelho', 'https://paulocoelho.com', N'Tác giả Nhà giả kim'),
(N'Dale Carnegie', NULL, N'Tác giả kỹ năng sống'),
(N'Robin Sharma', NULL, N'Nhà đào tạo lãnh đạo'),
(N'Napoleon Hill', NULL, N'Tác giả Think and Grow Rich'),
(N'James Clear', 'https://jamesclear.com', N'Tác giả Atomic Habits'),
(N'Yuval Noah Harari', NULL, N'Sử gia, triết học'),
(N'Stephen Hawking', NULL, N'Nhà khoa học'),

(N'Dan Brown', NULL, N'Viết tiểu thuyết trinh thám'),
(N'Agatha Christie', NULL, N'Nữ hoàng trinh thám'),
(N'Arthur Conan Doyle', NULL, N'Tác giả Sherlock Holmes'),
(N'Ernest Hemingway', NULL, N'Nhà văn Mỹ'),
(N'George Orwell', NULL, N'Tác giả 1984'),
(N'Leo Tolstoy', NULL, N'Tác giả Chiến tranh và Hòa bình'),
(N'Fyodor Dostoevsky', NULL, N'Nhà văn Nga'),
(N'Franz Kafka', NULL, N'Văn học hiện sinh'),
(N'Gabriel Garcia Marquez', NULL, N'Nhà văn Nobel'),
(N'Victor Hugo', NULL, N'Tác giả Những người khốn khổ'),

(N'Hồ Anh Thái', NULL, N'Nhà văn Việt Nam'),
(N'Bảo Ninh', NULL, N'Tác giả Nỗi buồn chiến tranh'),
(N'Nguyễn Ngọc Tư', NULL, N'Tác giả Cánh đồng bất tận'),
(N'Tô Hoài', NULL, N'Tác giả Dế Mèn phiêu lưu ký'),
(N'Nam Cao', NULL, N'Tác giả Chí Phèo'),
(N'Nguyễn Du', NULL, N'Tác giả Truyện Kiều'),
(N'Xuân Diệu', NULL, N'Nhà thơ mới'),
(N'Huy Cận', NULL, N'Nhà thơ'),
(N'Nguyễn Trí Huân', NULL, N'Nhà văn quân đội'),
(N'Mạc Ngôn', NULL, N'Nhà văn Trung Quốc'),

(N'Trương Gia Bình', NULL, N'Lãnh đạo doanh nghiệp'),
(N'Henry Ford', NULL, N'Nhà sáng lập Ford'),
(N'Elon Musk', NULL, N'CEO Tesla'),
(N'Bill Gates', NULL, N'Sáng lập Microsoft'),
(N'Steve Jobs', NULL, N'Sáng lập Apple'),
(N'Jack Ma', NULL, N'Sáng lập Alibaba'),
(N'Warren Buffett', NULL, N'Nhà đầu tư'),
(N'Peter Thiel', NULL, N'Nhà đầu tư công nghệ'),
(N'Mark Cuban', NULL, N'Doanh nhân Mỹ'),
(N'Reed Hastings', NULL, N'CEO Netflix'),

(N'Nguyễn Mạnh Tường', NULL, N'Bác sĩ – nhà văn'),
(N'Đặng Lê Nguyên Vũ', NULL, N'Doanh nhân Việt'),
(N'Hồ Quang Cua', NULL, N'Chuyên gia nông nghiệp'),
(N'Phan Văn Trường', NULL, N'Chuyên gia quản trị'),
(N'Nguyễn Hiến Lê', NULL, N'Học giả Việt Nam'),
(N'Dương Thu Hương', NULL, N'Nhà văn Việt Nam'),
(N'Phan Thị Vàng Anh', NULL, N'Nhà văn nữ'),
(N'Nguyễn Khải', NULL, N'Nhà văn thời kỳ đổi mới'),
(N'Murakami Ryu', NULL, N'Nhà văn Nhật'),
(N'Ishiguro Kazuo', NULL, N'Nhà văn Nobel'),

(N'Brené Brown', NULL, N'Tâm lý học ứng dụng'),
(N'Cal Newport', NULL, N'Tác giả Deep Work'),
(N'Daniel Kahneman', NULL, N'Kinh tế học hành vi'),
(N'Jordan Peterson', NULL, N'Tâm lý học'),
(N'Malcolm Gladwell', NULL, N'Tác giả Outliers'),
(N'Simon Sinek', NULL, N'Tác giả Start With Why'),
(N'Tony Robbins', NULL, N'Diễn giả truyền cảm hứng'),
(N'Brian Tracy', NULL, N'Kỹ năng bán hàng'),
(N'Zig Ziglar', NULL, N'Kỹ năng sống'),
(N'Eckhart Tolle', NULL, N'Tâm linh học'),

(N'Robert Kiyosaki', NULL, N'Tác giả Cha giàu cha nghèo'),
(N'Jeff Olson', NULL, N'Tác giả The Slight Edge'),
(N'Kevin Mitnick', NULL, N'An ninh mạng'),
(N'Andrew Ng', NULL, N'AI - Machine Learning'),
(N'Martin Fowler', NULL, N'Kiến trúc phần mềm'),
(N'Kent Beck', NULL, N'Extreme Programming'),
(N'Uncle Bob Martin', NULL, N'Clean Code'),
(N'Bjarne Stroustrup', NULL, N'Sáng lập C++'),
(N'Guido van Rossum', NULL, N'Sáng lập Python'),
(N'Dennis Ritchie', NULL, N'Sáng lập C language');
GO

-- thêm NXB
INSERT INTO NHAXUATBAN (TenNXB) VALUES
(N'Nhà xuất bản Giáo Dục Việt Nam'),
(N'Nhà xuất bản Trẻ'),
(N'Nhà xuất bản Kim Đồng'),
(N'Nhà xuất bản Lao Động'),
(N'Nhà xuất bản Văn Học'),
(N'Nhà xuất bản Tổng Hợp TP.HCM'),
(N'Nhà xuất bản Thanh Niên'),
(N'Nhà xuất bản Phụ Nữ'),
(N'Nhà xuất bản Chính Trị Quốc Gia'),
(N'Nhà xuất bản Khoa Học & Kỹ Thuật'),

(N'Nhà xuất bản Công Thương'),
(N'Nhà xuất bản Đại Học Quốc Gia Hà Nội'),
(N'Nhà xuất bản Đại Học Quốc Gia TP.HCM'),
(N'Nhà xuất bản Tài Chính'),
(N'Nhà xuất bản Giao Thông Vận Tải'),
(N'Nhà xuất bản Y Học'),
(N'Nhà xuất bản Quân Đội Nhân Dân'),
(N'Nhà xuất bản Mỹ Thuật'),
(N'Nhà xuất bản Âm Nhạc'),
(N'Nhà xuất bản Thông Tin & Truyền Thông'),

(N'Nhà xuất bản Xây Dựng'),
(N'Nhà xuất bản Nông Nghiệp'),
(N'Nhà xuất bản Thế Giới'),
(N'Nhà xuất bản Hồng Đức'),
(N'Nhà xuất bản Dân Trí'),
(N'Nhà xuất bản Tri Thức'),
(N'Nhà xuất bản Kinh Tế'),
(N'Nhà xuất bản Phương Đông'),
(N'Nhà xuất bản Tôn Giáo'),
(N'Nhà xuất bản Văn Hóa Dân Tộc'),

(N'NXB Ánh Dương'),
(N'NXB Sao Mai'),
(N'NXB Minh Long'),
(N'NXB Phương Nam'),
(N'NXB First News'),
(N'NXB Alpha Books'),
(N'NXB Skybooks'),
(N'NXB Nhã Nam'),
(N'NXB IPM'),
(N'NXB Bách Việt'),

(N'NXB Đông A'),
(N'NXB Bloom Books'),
(N'NXB Red Wheel'),
(N'NXB Sách Trí Việt'),
(N'NXB Triệu Triệu'),
(N'NXB Light Novel Việt'),
(N'NXB Học Thuật'),
(N'NXB Công Nghệ Thông Tin');
GO

-- Thêm BẠN ĐỌC
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

-- THÊM LỊCH LÀM GIẢ LẬP
DECLARE @NgayCoSo DATE = CAST(GETDATE() AS DATE);

INSERT INTO LICHLAM (EmailThuThu, Ngay, GioBatDau, GioKetThuc, TrangThai, GhiChu, CreatedBy)
VALUES
    ('thuthu1@thuvien.com', @NgayCoSo, '08:00', '12:00', 'Scheduled', N'Quầy mượn sách buổi sáng', 'admin@thuvien.com'),
    ('thuthu1@thuvien.com', DATEADD(DAY, 1, @NgayCoSo), '13:00', '17:00', 'Scheduled', N'Quầy trả sách', 'admin@thuvien.com'),
    ('thuthu2@thuvien.com', @NgayCoSo, '09:00', '12:00', 'Done', N'Xử lý yêu cầu gia hạn', 'admin@thuvien.com'),
    ('thuthu3@thuvien.com', DATEADD(DAY, 2, @NgayCoSo), '14:00', '18:00', 'Scheduled', N'Sắp xếp kho', 'admin@thuvien.com');
GO

-- THÊM THÔNG BÁO VÀ NGƯỜI NHẬN
DECLARE @ThongBaoId1 INT;
DECLARE @ThongBaoId2 INT;

INSERT INTO THONGBAO (TieuDe, NoiDung, CreatedBy)
VALUES (N'Lịch làm tuần mới', N'Đã cập nhật lịch làm việc cho tuần tới, vui lòng kiểm tra.', 'admin@thuvien.com');
SET @ThongBaoId1 = SCOPE_IDENTITY();

INSERT INTO THONGBAO (TieuDe, NoiDung, CreatedBy)
VALUES (N'Nhắc nhở trả sách', N'Vui lòng rà soát các bạn đọc sắp đến hạn trả sách.', 'thuthu1@thuvien.com');
SET @ThongBaoId2 = SCOPE_IDENTITY();

INSERT INTO THONGBAO_NGUOINHAN (IdThongBao, EmailNhan, DaDoc, ReadAt)
VALUES
    (@ThongBaoId1, 'thuthu1@thuvien.com', 1, DATEADD(HOUR, 2, SYSUTCDATETIME())),
    (@ThongBaoId1, 'thuthu2@thuvien.com', 0, NULL),
    (@ThongBaoId2, 'thuthu3@thuvien.com', 0, NULL),
    (@ThongBaoId2, 'thuthu4@thuvien.com', 0, NULL);
GO

-- THÊM YÊU CẦU RESET MẬT KHẨU
INSERT INTO YEUCAU_RESETMK (EmailThuThu, LyDo, TrangThai, XuLyBoi, XuLyLuc, GhiChuXuLy)
VALUES
    ('thuthu2@thuvien.com', N'Quên mật khẩu do đổi thiết bị', 'Done', 'admin@thuvien.com', DATEADD(DAY, -1, SYSUTCDATETIME()), N'Đã xác minh qua điện thoại'),
    ('thuthu4@thuvien.com', N'Bị khóa do nhập sai nhiều lần', 'Approved', 'thuthu1@thuvien.com', DATEADD(HOUR, -6, SYSUTCDATETIME()), N'Chờ đổi mật khẩu'),
    ('thuthu5@thuvien.com', N'Nghi ngờ tài khoản bị truy cập trái phép', 'Pending', NULL, NULL, NULL);
GO