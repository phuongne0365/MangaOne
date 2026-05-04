-- 1. Xóa Database cũ (nếu có) để làm lại từ đầu cho sạch
DROP DATABASE IF EXISTS mangaone;
CREATE DATABASE mangaone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mangaone;

-- ==========================================
-- PHẦN 1: TẠO BẢNG (Chuẩn khớp Spring Boot - ID kiểu BIGINT)
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

-- Cột id (BIGINT) khớp với Entity Java
CREATE TABLE MANGAS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(150),
    description TEXT,
    price FLOAT NOT NULL,
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

-- Khóa ngoại trỏ đến id (BIGINT) của bảng MANGAS
CREATE TABLE CART_ITEMS (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    manga_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    FOREIGN KEY (manga_id) REFERENCES MANGAS(id)
);

-- Khóa ngoại trỏ đến id (BIGINT) của bảng MANGAS và price kiểu FLOAT
CREATE TABLE ORDER_DETAILS (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    manga_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price FLOAT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    FOREIGN KEY (manga_id) REFERENCES MANGAS(id)
);

-- ==========================================
-- PHẦN 2: THÊM DỮ LIỆU ĐỒNG BỘ
-- ==========================================

-- 2.1 THÊM THỂ LOẠI
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

-- 2.2 THÊM NHÀ XUẤT BẢN
INSERT INTO PUBLISHERS (publisher_name) VALUES
('NXB Kim Đồng'),
('NXB Trẻ'),
('IPM'),
('Thái Hà Books'),
('Amak Books'),
('Skybooks Tsubasa'),
('NXB Hà Nội');

-- 2.3 THÊM 19 BỘ TRUYỆN CÙNG ĐƯỜNG DẪN ẢNH NGAY TỪ ĐẦU (Không cần câu lệnh UPDATE nữa)
INSERT INTO MANGAS (title, author, description, price, stock_quantity, image_url, category_id, publisher_id) VALUES
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

('Chainsaw Man', 'Tatsuki Fujimoto', 'Thiếu niên mang trong mình sức mạnh Quỷ Cưa.', 40000, 100, 'images/manga/chainsaw-man-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Attack on Titan (Đại Chiến Titan)', 'Hajime Isayama', 'Cuộc chiến sinh tồn trước loài Titan.', 35000, 80, 'images/manga/attach-on-titan-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Seinen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Dưới Ánh Hào Quang (Oshi no Ko)', 'Aka Akasaka', 'Góc khuất ngành công nghiệp giải trí idol.', 45000, 200, 'images/manga/oshi-no-koto-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Seinen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM')),

('Thất Nghiệp Chuyển Sinh (Mushoku Tensei)', 'Rifujin na Magonote', 'Hành trình làm lại cuộc đời ở thế giới phép thuật.', 50000, 90, 'images/manga/mushoku-tensei-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Isekai%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM')),

('One Piece (Đảo Hải Tặc)', 'Eiichiro Oda', 'Hành trình tìm kiếm kho báu huyền thoại One Piece.', 25000, 1000, 'images/manga/one-piece-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Death Note (Quyển Sổ Thiên Mệnh)', 'Tsugumi Ohba', 'Cuộc đấu trí thông qua quyển sổ tử thần.', 45000, 150, 'images/manga/death-note-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Bí ẩn%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Blue Lock', 'Muneyuki Kaneshiro', 'Dự án đào tạo tiền đạo ích kỷ nhất thế giới.', 35000, 200, 'images/manga/blue-lock-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Sports%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Blue Box (Hộp Xanh)', 'Kouji Miura', 'Câu chuyện tình cảm học đường nhẹ nhàng.', 30000, 180, 'images/manga/box-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Romance%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Bleach', 'Tite Kubo', 'Hành trình làm Thần Chết và bảo vệ thế giới của Ichigo.', 35000, 100, 'images/manga/bleach-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Black Clover', 'Yuki Tabata', 'Cậu bé không có phép thuật và giấc mơ trở thành Ma Pháp Vương.', 38000, 90, 'images/manga/black-clover-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Fullmetal Alchemist', 'Hiromu Arakawa', 'Hai anh em nhà Elric và hành trình tìm lại cơ thể.', 42000, 80, 'images/manga/fullmetal-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Phiêu lưu%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Tokyo Revengers', 'Ken Wakui', 'Xuyên không thay đổi quá khứ và cứu lấy người yêu.', 35000, 150, 'images/manga/tokyo-revengers-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Dr. STONE', 'Riichiro Inagaki', 'Khôi phục nền văn minh nhân loại bằng sức mạnh khoa học.', 32000, 110, 'images/manga/dr-stone-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Kaguya-sama: Love Is War', 'Aka Akasaka', 'Cuộc chiến tỏ tình giữa hai thiên tài trường học.', 36000, 100, 'images/manga/kaguya-sama-tap1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Romance%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM'));

-- 2.4 THÊM TÀI KHOẢN (Đảm bảo tài khoản được thêm vào trước khi gán dữ liệu test)
INSERT INTO USERS (email, password, full_name, phone_number, address, role) VALUES
('admin@mangaone.com', '123456', 'Quản Trị Viên', '0987654321', 'Trụ sở chính', 'ADMIN'),
('duyson@gmail.com', '123456', 'Đào Duy Sơn', '0912345678', 'Hà Nội', 'USER'),
('bichngoc@gmail.com', '123456', 'Nguyễn Thị Bích Ngọc', '0909090909', 'Hải Phòng', 'USER'),
('phuong@gmail.com', '123456', 'Trưởng Nhóm Phượng', '0988888888', 'Đà Nẵng', 'USER'),
('admin1@mangaone.com', '123456', 'Vu Phuong', '0901000001', 'Ha Noi', 'ADMIN'),
('admin2@mangaone.com', '123456', 'Dao Duy Son', '0901000002', 'Ha Noi', 'ADMIN'),
('admin3@mangaone.com', '123456', 'Nguyen Thi Bich Ngoc', '0901000003', 'Ha Noi', 'ADMIN'),
('admin4@mangaone.com', '123456', 'Tran Hoang Duy', '0901000004', 'Ha Noi', 'ADMIN'),
('admin5@mangaone.com', '123456', 'Dam Minh Hieu', '0901000005', 'Ha Noi', 'ADMIN'),
('user1@mangaone.com', '123456', 'Nguoi Dung 1', '0902000001', 'Ha Noi', 'USER'),
('user2@mangaone.com', '123456', 'Nguoi Dung 2', '0902000002', 'Ha Noi', 'USER');

-- 2.5 THÊM GIỎ HÀNG VÀ ĐƠN HÀNG MẪU ĐỂ TEST
INSERT INTO CART_ITEMS (user_id, manga_id, quantity) VALUES
((SELECT user_id FROM USERS WHERE email = 'duyson@gmail.com'), (SELECT id FROM MANGAS WHERE title LIKE '%Chú Thuật Hồi Chiến%' LIMIT 1), 2),
((SELECT user_id FROM USERS WHERE email = 'duyson@gmail.com'), (SELECT id FROM MANGAS WHERE title LIKE '%Chainsaw Man%' LIMIT 1), 1);

INSERT INTO ORDERS (user_id, receiver_name, receiver_phone, shipping_address, total_amount, status) VALUES
((SELECT user_id FROM USERS WHERE email = 'bichngoc@gmail.com'), 'Nguyễn Thị Bích Ngọc', '0909090909', 'Số 1, Lê Lợi, Hải Phòng', 69000, 'SHIPPING');

INSERT INTO ORDER_DETAILS (order_id, manga_id, quantity, price) VALUES
((SELECT order_id FROM ORDERS WHERE receiver_name = 'Nguyễn Thị Bích Ngọc' LIMIT 1), (SELECT id FROM MANGAS WHERE title LIKE '%Spy x Family%' LIMIT 1), 1, 25000),
((SELECT order_id FROM ORDERS WHERE receiver_name = 'Nguyễn Thị Bích Ngọc' LIMIT 1), (SELECT id FROM MANGAS WHERE title LIKE '%Conan%' LIMIT 1), 2, 22000);

-- 3. KIỂM TRA DỮ LIỆU ĐÃ TẠO
SELECT * FROM MANGAS;
SELECT * FROM CATEGORIES;
SELECT * FROM PUBLISHERS;
SELECT * FROM USERS;