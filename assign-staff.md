# Đề thi thực hành Lập trình mạng

# Thời gian 60 phút.

# Được sử dụng tài liệu

**Đề bài: Phân công cán bộ coi thi**

Cho n phòng thi (n>=1000 phòng thi), m giám thi. Mỗi phòng thi luôn có 2 giám thị (giám thị 1 và giám thị 2). Số giám thị còn thừa là làm cán bộ giám sát hành lang (số cán bộ giám sát hành lang được chia đều cho tất cả các phòng thi). Ca đầu tiên mỗi phòng 02 giám thị,với ca thi tiếp theo thì các giám thị lần đầu tiên không được xem thi ở phòng của ca đầu tiên và không không được trùng cán bộ kia, và cứ thực hiện không được trùng cho các ca thi tiếp theo. (các cán bộ giám sát vẫn được xếp các giám thị cho các lần tiếp). Hãy viết chương trình thực hiện các các yêu cầu sau:

- Đọc file danh sách cán bộ coi thi CANBOCOITHI.XLSX có cấu trúc như sau

| STT | Họ và tên  | Ngày sinh  | Mã cán bộ | Đơn vị công tác |
| --- | ---------- | ---------- | --------- | --------------- |
| 01  | Nguyễn Anh | 09/04/1961 | GV1       | ĐHBK            |
| 02  | Nguyễn Bê  | 07/09/1979 | GV2       | ĐHKT            |
| ….. |            |            |           |                 |

- Đọc file danh sách phòng PHONGTHI.XLSX có cấu trúc như sau

| STT | Phòng thi | Địa điểm |
| --- | --------- | -------- |
| 01  | 128       | Đà Nẵng  |
| 02  | 151       | Huế      |
| ….. |           |          |

- Xuất ra danh sách có tên file DANHSACH PHANCONG.XLSX có cấu trúc như sau:

| STT | Mã GV     | Họ và tên     | GIÁM THỊ   |            | Phòng thi |
| --- | --------- | ------------- | ---------- | ---------- | --------- |
|     |           |               | Giám thi 1 | Giám thị 2 |           |
| 01  | 105150103 | Võ Năm        | X          |            | 128       |
| 02  | 101170014 | Trần Hưng Đức |            | X          | C102      |
| ….. |           |               |            |            |           |

- Xuất ra danh sách có tên file DANHSACH GIAMSAT.XLSX có cấu trúc như sau:

| STT | Mã GV | Họ và tên  | Phòng thi được giám sát |
| --- | ----- | ---------- | ----------------------- |
| 01  | GV3   | Nguyễn Bôn | Từ C101 đến C110        |
| 02  | GV7   | Lê Cam     | Từ C202-C209            |
| ….. |       |            |                         |

▪ Đầu vào: Danh sách cán bộ coi thi và danh sách phòng thi (có dạng như trình bày theo hai bảng CANBOCOITHI.XLSX và PHONGTHI.XLSX)

▪ Đầu ra: Danh sách phân công giám thị coi thi ( theo mẫu DANHSACHPHANCONG.XLSX, DANHSACHGIAMSAT.XLSX )

- Đầu vào : Chọn số người và số phòng.
