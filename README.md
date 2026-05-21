# Exam Scheduler

He thong phan cong giam thi theo mo hinh `dataset + branch`.

## Kien truc

| Service | Port mac dinh | Vai tro |
|---|---:|---|
| `assign-server` | `8081` | API backend, luu MySQL, quan ly dataset/branch, sinh ca thi |
| `client-web` | `8080` | Giao dien JSP de upload dataset, tao branch, tao ca tiep, reset, export |

## Yeu cau

- Java 17+
- Maven 3.9+
- Node.js 18+ chi dung cho Playwright test

## Bien moi truong

| Bien | Mac dinh | Y nghia |
|---|---|---|
| `BACKEND_PORT` | `8081` | Port cua `assign-server` |
| `FRONTEND_PORT` | `8080` | Port cua `client-web` |
| `SERVER_BASE_URL` | `http://localhost:8081/assign-server` | URL frontend goi backend |
| `ASSIGN_SERVER_DATA_DIR` | `assign-server-data` | Thu muc tam cho file runtime/output |
| `ASSIGN_DB_URL` | `jdbc:mysql://localhost:3306/exam_scheduler?...` | Chuoi ket noi MySQL |
| `ASSIGN_DB_USER` | `root` | Tai khoan MySQL |
| `ASSIGN_DB_PASSWORD` | rong | Mat khau MySQL |
| `BIND_HOST` | `0.0.0.0` | Host bind cho script deploy |

Neu ton tai file `.env` o root repo, `just` va script deploy se tu nap.

Can tao san database MySQL, vi backend chi tu tao bang, khong tu tao database.

## Chay project

Backend:

```bash
mvn -q -f assign-server/pom.xml clean compile
mvn -f assign-server/pom.xml jetty:run -Djetty.http.port=8081 -Dassign.dataDir=assign-server-data -Dassign.dbUrl="jdbc:mysql://localhost:3306/exam_scheduler?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8" -Dassign.dbUser=root -Dassign.dbPassword=
```

Frontend o terminal khac:

```bash
mvn -q -f client-web/pom.xml clean compile -DserverBaseUrl=http://localhost:8081/assign-server
mvn -f client-web/pom.xml jetty:run -Djetty.http.port=8080 -DserverBaseUrl=http://localhost:8081/assign-server
```

Hoac dung:

```bash
just backend
just frontend
```

Mo `http://localhost:8080`.

## Luong su dung

1. Upload `staff dataset`
2. Upload `room dataset`
3. Tao `branch` moi voi:
   - dataset staff
   - dataset room
   - so giam thi
   - so phong thi
4. He thong tu sinh `ca 1`
5. Moi lan bam `Tao ca tiep` se sinh them 1 ca moi cho branch do
6. `Reset` tao branch moi tu ca 1, branch cu van duoc luu
7. Co the export:
   - `DANHSACH_PHANCONG.xlsx`
   - `DANHSACH_GIAMSAT.xlsx`

## Rang buoc lap lich

- Moi phong co dung 2 giam thi
- Giam thi du lam giam sat hanh lang, chia deu theo phong
- Trong cung 1 branch:
  - giam thi khong quay lai phong da tung coi
  - cap giam thi khong duoc lap lai
- Neu `so giam thi` nho hon file, he thong chon ngau nhien tap con
- Neu `so phong thi` nho hon file, he thong lay phong tu tren xuong
- Neu branch khong con kha nang sinh ca hop le, he thong bao loi va goi y reset

## API chinh

- `POST /api/staff-datasets`
- `GET /api/staff-datasets`
- `POST /api/staff-datasets/{id}/archive`
- `POST /api/room-datasets`
- `GET /api/room-datasets`
- `POST /api/room-datasets/{id}/archive`
- `POST /api/branches`
- `GET /api/branches`
- `GET /api/branches/{id}`
- `POST /api/branches/{id}/sessions`
- `POST /api/branches/{id}/reset`
- `POST /api/branches/{id}/archive`
- `GET /api/branches/{id}/sessions/{no}`
- `GET /api/branches/{id}/preview`
- `GET /api/branches/{id}/downloads/invigilators`
- `GET /api/branches/{id}/downloads/monitors`

## Input Excel

Staff file chap nhan ca mau cu va moi:

- `STT | Họ và tên | Ngày sinh | Mã cán bộ | Đơn vị công tác`
- `STT | Mã GV | Họ Tên | Ngày sinh | Đơn vị công tác`

Room file chap nhan:

- `STT | Phòng thi | Địa điểm`
- `STT | Phòng thi | Ghi chú`

## Test

```bash
mvn test
```
