-- Xóa Database cũ (nếu có) để làm lại từ đầu cho sạch
DROP DATABASE IF EXISTS mangaone;
CREATE DATABASE mangaone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mangaone;

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
    manga_id BIGINT PRIMARY KEY AUTO_INCREMENT,
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

CREATE TABLE CART_ITEMS (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    manga_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    FOREIGN KEY (manga_id) REFERENCES MANGAS(manga_id)
);

CREATE TABLE ORDER_DETAILS (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    manga_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price FLOAT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    FOREIGN KEY (manga_id) REFERENCES MANGAS(manga_id)
);

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

INSERT INTO PUBLISHERS (publisher_name) VALUES
('NXB Kim Đồng'),
('NXB Trẻ'),
('IPM'),
('Thái Hà Books'),
('Amak Books'),
('Skybooks Tsubasa'),
('NXB Hà Nội');

-- ✅ CHỈ SỬA PHẦN image_url: đổi từ đường dẫn local sang URL thật
INSERT INTO MANGAS (title, author, description, price, stock_quantity, image_url, category_id, publisher_id) VALUES
('Chú Thuật Hồi Chiến (Jujutsu Kaisen)', 'Gege Akutami', 'Hành trình của Yuji Itadori bước vào thế giới Chú Thuật Sư.', 30000, 150, 'https://m.media-amazon.com/images/I/81qPzeEO5IL._SL1500_.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Spy x Family', 'Tatsuya Endo', 'Gia đình giả của điệp viên, sát thủ và cô bé ngoại cảm.', 25000, 250, 'https://cdn.myanimelist.net/images/manga/1/267793.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Comedy%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Thanh Gươm Diệt Quỷ (Demon Slayer)', 'Koyoharu Gotouge', 'Hành trình diệt quỷ của Tanjiro.', 25000, 300, 'https://cdn.myanimelist.net/images/manga/3/179023.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Frieren - Pháp Sư Tiễn Táng', 'Kanehito Yamada', 'Chuyến hành trình chiêm nghiệm của Elf Frieren.', 35000, 120, 'https://cdn.myanimelist.net/images/manga/3/188896.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Slice of Life%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Thám Tử Lừng Danh Conan', 'Gosho Aoyama', 'Học sinh trung học Shinichi bị teo nhỏ.', 22000, 500, 'https://cdn.myanimelist.net/images/manga/3/188896l.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Chainsaw Man', 'Tatsuki Fujimoto', 'Thiếu niên mang trong mình sức mạnh Quỷ Cưa.', 40000, 100, 'https://cdn.myanimelist.net/images/manga/3/216464.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Attack on Titan (Đại Chiến Titan)', 'Hajime Isayama', 'Cuộc chiến sinh tồn trước loài Titan.', 35000, 80, 'https://cdn.myanimelist.net/images/manga/2/37846.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Seinen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Dưới Ánh Hào Quang (Oshi no Ko)', 'Aka Akasaka', 'Góc khuất ngành công nghiệp giải trí idol.', 45000, 200, 'https://cdn.myanimelist.net/images/manga/3/249658.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Seinen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM')),

('Thất Nghiệp Chuyển Sinh (Mushoku Tensei)', 'Rifujin na Magonote', 'Hành trình làm lại cuộc đời ở thế giới phép thuật.', 50000, 90, 'https://cdn.myanimelist.net/images/manga/1/157897.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Isekai%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM')),

('One Piece (Đảo Hải Tặc)', 'Eiichiro Oda', 'Hành trình tìm kiếm kho báu huyền thoại One Piece.', 25000, 1000, 'https://cdn.myanimelist.net/images/manga/2/253146.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Death Note (Quyển Sổ Thiên Mệnh)', 'Tsugumi Ohba', 'Cuộc đấu trí thông qua quyển sổ tử thần.', 45000, 150, 'https://cdn.myanimelist.net/images/manga/1/258245.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Bí ẩn%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Blue Lock', 'Muneyuki Kaneshiro', 'Dự án đào tạo tiền đạo ích kỷ nhất thế giới.', 35000, 200, 'https://cdn.myanimelist.net/images/manga/2/253146.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Sports%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Blue Box (Hộp Xanh)', 'Kouji Miura', 'Câu chuyện tình cảm học đường nhẹ nhàng.', 30000, 180, 'https://cdn.myanimelist.net/images/manga/2/279463.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Romance%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Bleach', 'Tite Kubo', 'Hành trình làm Thần Chết và bảo vệ thế giới của Ichigo.', 35000, 100, 'https://m.media-amazon.com/images/I/91D07epNE9L._SL1500_.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Black Clover', 'Yuki Tabata', 'Cậu bé không có phép Zthuật và giấc mơ trở thành Ma Pháp Vương.', 38000, 90, 'https://m.media-amazon.com/images/I/91dSMhdIzTL._SL1500_.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Fullmetal Alchemist', 'Hiromu Arakawa', 'Hai anh em nhà Elric và hành trình tìm lại cơ thể.', 42000, 80, 'https://cdn.myanimelist.net/images/manga/3/243675.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Phiêu lưu%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Tokyo Revengers', 'Ken Wakui', 'Xuyên không thay đổi quá khứ và cứu lấy người yêu.', 35000, 150, 'https://cdn.myanimelist.net/images/manga/3/214566.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Trẻ')),

('Dr. STONE', 'Riichiro Inagaki', 'Khôi phục nền văn minh nhân loại bằng sức mạnh khoa học.', 32000, 110, 'https://upload.wikimedia.org/wikipedia/en/9/94/NarutoCoverTankobon1.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Shonen%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'NXB Kim Đồng')),

('Kaguya-sama: Love Is War', 'Aka Akasaka', 'Cuộc chiến tỏ tình giữa hai thiên tài trường học.', 36000, 100, 'https://cdn.myanimelist.net/images/manga/2/37846.jpg', 
    (SELECT category_id FROM CATEGORIES WHERE category_name LIKE '%Romance%'), (SELECT publisher_id FROM PUBLISHERS WHERE publisher_name = 'IPM'));

INSERT INTO USERS (email, password, full_name, phone_number, address, role) VALUES
('duyson@gmail.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Đào Duy Sơn', '0912345678', 'Hà Nội', 'USER'),
('bichngoc@gmail.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Nguyễn Thị Bích Ngọc', '0909090909', 'Hải Phòng', 'USER'),
('phuong@gmail.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Trưởng Nhóm Phượng', '0988888888', 'Đà Nẵng', 'USER'),
('admin1@mangaone.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Vu Phuong', '0901000001', 'Ha Noi', 'ADMIN'),
('admin2@mangaone.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Dao Duy Son', '0901000002', 'Ha Noi', 'ADMIN'),
('admin3@mangaone.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Nguyen Thi Bich Ngoc', '0901000003', 'Ha Noi', 'ADMIN'),
('admin4@mangaone.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Tran Hoang Duy', '0901000004', 'Ha Noi', 'ADMIN'),
('admin5@mangaone.com', '$2a$12$cWLY0ngg2.Qt99p2HH9bTOzimfFVJQ0AOyNlD5HljIEY.A4jLlDQm', 'Dam Minh Hieu', '0901000005', 'Ha Noi', 'ADMIN');

INSERT INTO CART_ITEMS (user_id, manga_id, quantity) VALUES
((SELECT user_id FROM USERS WHERE email = 'duyson@gmail.com'), (SELECT manga_id FROM MANGAS WHERE title LIKE '%Chú Thuật Hồi Chiến%' LIMIT 1), 2),
((SELECT user_id FROM USERS WHERE email = 'duyson@gmail.com'), (SELECT manga_id FROM MANGAS WHERE title LIKE '%Chainsaw Man%' LIMIT 1), 1);

INSERT INTO ORDERS (user_id, receiver_name, receiver_phone, shipping_address, total_amount, status) VALUES
((SELECT user_id FROM USERS WHERE email = 'bichngoc@gmail.com'), 'Nguyễn Thị Bích Ngọc', '0909090909', 'Số 1, Lê Lợi, Hải Phòng', 69000, 'SHIPPING');

INSERT INTO ORDER_DETAILS (order_id, manga_id, quantity, price) VALUES
((SELECT order_id FROM ORDERS WHERE receiver_name = 'Nguyễn Thị Bích Ngọc' LIMIT 1), (SELECT manga_id FROM MANGAS WHERE title LIKE '%Spy x Family%' LIMIT 1), 1, 25000),
((SELECT order_id FROM ORDERS WHERE receiver_name = 'Nguyễn Thị Bích Ngọc' LIMIT 1), (SELECT manga_id FROM MANGAS WHERE title LIKE '%Conan%' LIMIT 1), 2, 22000);

SELECT * FROM MANGAS;
SELECT * FROM CATEGORIES;
SELECT * FROM PUBLISHERS;
SELECT * FROM USERS;


-- Kiểm tra còn bao nhiêu
SELECT COUNT(*) FROM MANGAS;