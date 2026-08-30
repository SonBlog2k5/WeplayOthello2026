# WePlay Othello Android — GitHub build-ready

Bản Android này gồm MediaProjection, overlay nhỏ/mờ/kéo được, nhận diện bàn 8x8 theo vùng chuẩn hóa, và thuật toán Othello chọn nước ăn nhiều quân nhất. Không dùng Accessibility và không tự bấm.

## Build APK ngay trên điện thoại
1. Tạo một repository GitHub mới, ví dụ `weplay-othello`.
2. Upload toàn bộ nội dung thư mục này (không upload ZIP lồng trong ZIP).
3. Vào **Actions** → chọn **Build Android APK** → **Run workflow**.
4. Khi workflow xanh, mở run đó → phần **Artifacts** → tải `WePlayOthello-debug-apk`.
5. Giải nén artifact, mở `app-debug.apk` trên Android để cài.

Workflow dùng GitHub-hosted runner và Gradle để build APK. GitHub hỗ trợ chạy workflow thủ công và lưu file build dưới dạng artifact.

## Lưu ý
- Đây là debug APK, chưa ký để phát hành Play Store.
- Trên Android lần đầu phải cấp quyền **Hiển thị trên ứng dụng khác** và quyền **Ghi/chia sẻ màn hình**.
- Nhận diện WePlay phụ thuộc tỷ lệ/bố cục màn hình. Nếu vị trí bàn khác ảnh tham chiếu, cần hiệu chỉnh vùng board.
