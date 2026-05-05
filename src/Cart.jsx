import React, { useState, useEffect } from 'react';

function Cart() {
    const [items, setItems] = useState([]); // Lưu danh sách từ Backend
    const userId = 1; // Giả định ID của Phượng là 1

    useEffect(() => {
        // Gọi API từ Spring Boot
        fetch(`http://localhost:8080/api/cart/${userId}`)
            .then(res => res.json())
            .then(data => setItems(data))
            .catch(err => console.error("Lỗi kết nối Backend:", err));
    }, []);

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial' }}>
            <h2>🛒 Giỏ hàng MangaOne</h2>
            <table border="1" cellPadding="10" style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead style={{ backgroundColor: '#f4f4f4' }}>
                    <tr>
                        <th>Tên Truyện</th>
                        <th>Số lượng</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    {items.map((item) => (
                        <tr key={item.cartId}>
                            <td>{item.manga?.title || "Chưa có tên"}</td>
                            <td>{item.quantity}</td>
                            <td><button style={{ color: 'red' }}>Xóa</button></td>
                        </tr>
                    ))}
                    {items.length === 0 && <tr><td colSpan="3" style={{ textAlign: 'center' }}>Giỏ hàng đang trống</td></tr>}
                </tbody>
            </table>
        </div>
    );
}

export default Cart;