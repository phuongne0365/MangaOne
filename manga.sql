-- 1. Xóa Database cũ (nếu có) để làm lại từ đầu cho sạch
DROP DATABASE IF EXISTS mangaone;
CREATE DATABASE mangaone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mangaone;

-- ==========================================
-- PHẦN 1: TẠO BẢNG (Giữ nguyên chuẩn cấu trúc của nhóm)
-- ==========================================
CREATE TABLE USERS (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    role VARCHAR(50) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE CATEGORIES (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE PUBLISHERS (
    publisher_id INT PRIMARY KEY AUTO_INCREMENT,
    publisher_name VARCHAR(150) NOT NULL
);

CREATE TABLE MANGAS (
    manga_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(150),
    description TEXT,
    price INT NOT NULL,
    stock_quantity INT DEFAULT 0,
    image_url VARCHAR(500),
    category_id INT,
    publisher_id INT,
    FOREIGN KEY (category_id) REFERENCES CATEGORIES(category_id),
    FOREIGN KEY (publisher_id) REFERENCES PUBLISHERS(publisher_id)
);

CREATE TABLE ORDERS (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    total_amount INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);

CREATE TABLE CART_ITEMS (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    manga_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    FOREIGN KEY (manga_id) REFERENCES MANGAS(manga_id)
);

CREATE TABLE ORDER_DETAILS (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    manga_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    FOREIGN KEY (manga_id) REFERENCES MANGAS(manga_id)
);

-- ==========================================
-- PHẦN 2: THÊM DỮ LIỆU ĐỒNG BỘ
-- ==========================================

-- 2.1 TỔNG HỢP TOÀN BỘ THỂ LOẠI (Đã bổ sung Sports và Romance)
INSERT INTO CATEGORIES (category_name, description) VALUES
('Shonen (Thiếu niên)', 'Truyện tranh hành động, phiêu lưu, đề cao tình bạn và nỗ lực.'),
('Seinen (Trưởng thành)', 'Nội dung sâu sắc, tâm lý, kịch tính dành cho người trưởng thành.'),
('Comedy (Hài hước)', 'Truyện tranh mang tính chất giải trí, gây cười nhẹ nhàng.'),
('Slice of Life (Đời thường)', 'Khắc họa cuộc sống thường nhật, mang lại cảm giác bình yên.'),
('Isekai (Chuyển sinh)', 'Nhân vật chính xuyên không đến thế giới phép thuật, kỳ ảo.'),
('Sports (Thể thao)', 'Truyện tranh về đề tài thể thao, nhiệt huyết tuổi trẻ.'),
('Romance (Tình cảm)', 'Truyện tranh lãng mạn, tình yêu đôi lứa.'),
('Kinh dị (Horror)', 'Những câu chuyện rùng rợn, ám ảnh và kịch tính.'),
('Bí ẩn (Mystery)', 'Tập trung vào giải quyết các vụ án hoặc bí ẩn chưa có lời giải.'),
('Tâm lý (Psychological)', 'Khai thác sâu vào nội tâm và diễn biến tâm lý nhân vật.'),
('Phiêu lưu (Adventure)', 'Những chuyến hành trình dài đến các vùng đất mới.');

-- 2.2 TỔNG HỢP TOÀN BỘ NHÀ XUẤT BẢN
INSERT INTO PUBLISHERS (publisher_name) VALUES
('NXB Kim Đồng'),
('NXB Trẻ'),
('IPM'),
('Thái Hà Books'),
('Amak Books'),
('Skybooks Tsubasa'),
('NXB Hà Nội');

-- 2.3 ĐỒNG BỘ TOÀN BỘ TRUYỆN (Sử dụng 100% Subquery để tự tìm đúng ID + Sửa lại link ảnh chuẩn)
INSERT INTO MANGAS (title, author, description, price, stock_quantity, image_url, category_id, publisher_id) VALUES

-- Nhóm 1: Truyện Kim Đồng
('Chú Thuật Hồi Chiến (Jujutsu Kaisen)', 'Gege Akutami', 'Hành trình của Yuji Itadori bước vào thế giới Chú Thuật Sư.', 30000, 150, 'images/manga/jujutsu-kaisen-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Spy x Family', 'Tatsuya Endo', 'Gia đình giả của điệp viên, sát thủ và cô bé ngoại cảm.', 25000, 250, 'images/manga/spy-family-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Comedy%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Thanh Gươm Diệt Quỷ (Demon Slayer)', 'Koyoharu Gotouge', 'Hành trình diệt quỷ của Tanjiro.', 25000, 300, 'images/manga/demon-slayer-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Frieren - Pháp Sư Tiễn Táng', 'Kanehito Yamada', 'Chuyến hành trình chiêm nghiệm của Elf Frieren.', 35000, 120, 'images/manga/frieren-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Slice of Life%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Thám Tử Lừng Danh Conan', 'Gosho Aoyama', 'Học sinh trung học Shinichi bị teo nhỏ.', 22000, 500, 'images/manga/conan-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

-- Nhóm 2: NXB Trẻ & IPM
('Chainsaw Man', 'Tatsuki Fujimoto', 'Thiếu niên mang trong mình sức mạnh Quỷ Cưa.', 40000, 100, 'images/manga/chainsaw-man-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Attack on Titan (Đại Chiến Titan)', 'Hajime Isayama', 'Cuộc chiến sinh tồn trước loài Titan.', 35000, 80, 'images/manga/attach-on-titan-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Seinen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Dưới Ánh Hào Quang (Oshi no Ko)', 'Aka Akasaka', 'Góc khuất ngành công nghiệp giải trí idol.', 45000, 200, 'images/manga/oshi-no-koto-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Seinen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM')),

('Thất Nghiệp Chuyển Sinh (Mushoku Tensei)', 'Rifujin na Magonote', 'Hành trình làm lại cuộc đời ở thế giới phép thuật.', 50000, 90, 'images/manga/mushoku-tensei-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Isekai%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM')),

-- Nhóm 3: Các siêu phẩm bổ sung đợt 2
('One Piece (Đảo Hải Tặc)', 'Eiichiro Oda', 'Hành trình tìm kiếm kho báu huyền thoại One Piece.', 25000, 1000, 'images/manga/one-piece-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Death Note (Quyển Sổ Thiên Mệnh)', 'Tsugumi Ohba', 'Cuộc đấu trí thông qua quyển sổ tử thần.', 45000, 150, 'images/manga/death-note-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Bí ẩn%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Blue Lock', 'Muneyuki Kaneshiro', 'Dự án đào tạo tiền đạo ích kỷ nhất thế giới.', 35000, 200, 'images/manga/blue-lock-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Sports%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Blue Box (Hộp Xanh)', 'Kouji Miura', 'Câu chuyện tình cảm học đường nhẹ nhàng.', 30000, 180, 'images/manga/box-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Romance%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng'));
    

INSERT INTO USERS (email, password, full_name, phone_number, address, role) VALUES
('admin@mangaone.com', '123456', 'Quản Trị Viên', '0987654321', 'Trụ sở chính', 'ADMIN'),
('duyson@gmail.com', '123456', 'Đào Duy Sơn', '0912345678', 'Hà Nội', 'USER'),
('bichngoc@gmail.com', '123456', 'Nguyễn Thị Bích Ngọc', '0909090909', 'Hải Phòng', 'USER'),
('phuong@gmail.com', '123456', 'Trưởng Nhóm Phượng', '0988888888', 'Đà Nẵng', 'USER');
    USE mangaone;

-- Cập nhật đường dẫn ảnh cục bộ cho 13 bộ truyện
UPDATE MANGAS SET image_url = 'images/manga/attach-on-titan-tap1.jpg' WHERE title LIKE '%Attack on Titan%';
UPDATE MANGAS SET image_url = 'images/manga/blue-lock-tap1.jpg' WHERE title LIKE '%Blue Lock%';
UPDATE MANGAS SET image_url = 'images/manga/box-tap1.jpg' WHERE title LIKE '%Blue Box%';
UPDATE MANGAS SET image_url = 'images/manga/chainsaw-man-tap1.jpg' WHERE title LIKE '%Chainsaw Man%';
UPDATE MANGAS SET image_url = 'images/manga/conan-tap1.jpg' WHERE title LIKE '%Conan%';
UPDATE MANGAS SET image_url = 'images/manga/death-note-tap1.jpg' WHERE title LIKE '%Death Note%';
UPDATE MANGAS SET image_url = 'images/manga/demon-slayer-tap1.jpg' WHERE title LIKE '%Thanh Gươm Diệt Quỷ%';
UPDATE MANGAS SET image_url = 'images/manga/frieren-tap1.jpg' WHERE title LIKE '%Frieren%';
UPDATE MANGAS SET image_url = 'images/manga/jujutsu-kaisen-tap1.jpg' WHERE title LIKE '%Chú Thuật Hồi Chiến%';
UPDATE MANGAS SET image_url = 'images/manga/mushoku-tensei-tap1.jpg' WHERE title LIKE '%Thất Nghiệp Chuyển Sinh%';
UPDATE MANGAS SET image_url = 'images/manga/one-piece-tap1.jpg' WHERE title LIKE '%One Piece%';
UPDATE MANGAS SET image_url = 'images/manga/oshi-no-koto-tap1.jpg' WHERE title LIKE '%Oshi no Ko%';
UPDATE MANGAS SET image_url = 'images/manga/spy-family-tap1.jpg' WHERE title LIKE '%Spy x Family%';
select * from MANGAS;
select * from CATEGORIES;
SELECT * FROM PUBLISHERS;