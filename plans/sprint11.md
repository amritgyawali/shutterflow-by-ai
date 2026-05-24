# ShutterFlow: Sprint 11 Plan — Gallery & Photo Delivery

## 🎯 Sprint Goal
Construct a premium photo delivery and secure web gallery system. The system must support bulk photo uploads to AWS S3, automatic multi-resolution image thumbnail generation, multi-album gallery organizing structures, secure password protections, customizable watermark overlays using studio branding, multiple download formats (original, print-ready, web-ready), automatic zip compiling pipelines, and complete download tracking logs.

---

## 🛠️ Tech Stack & Services
- **Storage Service**: AWS S3 (handling original assets and thumbnails).
- **Asset Processing**: Java AWT / ImageIO or a dedicated native library for resizing and watermarking.
- **Archive Utilities**: Java Zip filesystem stream compiling.
- **Relational Datastore**: MySQL 8.x tracking gallery items and download logs.

---

## 📊 Bulk Upload & Photo Resizing Flow

```mermaid
graph TD
    Upload[Bulk Upload: POST /galleries/{id}/upload] --> S3Raw[Store Original Image in S3]
    S3Raw --> Resize{Trigger Async Resizing Worker}
    Resize --> Thumbnail[Generate 300px Thumbnail]
    Resize --> WebResolution[Generate 1080px Web Image]
    Resize --> Watermark[Apply Studio Branding Watermark Overlay]
    Thumbnail --> S3Web[Store Resized Assets in S3]
    WebResolution --> S3Web
    Watermark --> S3Web
    S3Web --> DB[Update Database Gallery Records]
```

---

## 📅 Day-by-Day (Daily) Detailed Plan

### 📌 Day 1: Gallery Core Schema & Models
- **Goal**: Model web galleries and define database tables.
- **Technical Steps**:
  - Implement `Gallery.java` JPA entity.
  - Link galleries to `Studio` and `Client` entities under strict tenancy boundaries.
  - Include fields: title, slug, password protection, expiration date, watermark settings, and status (DRAFT, ACTIVE, EXPIRED).

### 📌 Day 2: Multi-Album Structure
- **Goal**: Allow photographers to organize galleries into sections (e.g., "Ceremony", "Reception").
- **Technical Steps**:
  - Implement `GallerySection.java` entity, mapping to a `@OneToMany` collection in `Gallery`.
  - Implement `GalleryPhoto.java` mapping S3 keys, sizes, section linkages, and order.

### 📌 Day 3: Bulk Photo Upload to AWS S3
- **Goal**: Stream uploads directly into structured S3 buckets.
- **Technical Steps**:
  - Create a multi-part upload controller `/galleries/{id}/upload`.
  - Save original high-resolution assets into S3 path `/studios/{studioId}/galleries/{galleryId}/raw/`.

### 📌 Day 4: Asynchronous Thumbnail Generator
- **Goal**: Automatically resize uploaded pictures to optimize page load speeds.
- **Technical Steps**:
  - Use Spring `@Async` or an event-driven listener to trigger thumbnail processing in background threads.
  - Generate thumbnails (300px width) and web-ready copies (1080px width) using Java ImageIO, and save back to S3.

### 📌 Day 5: Watermark Overlay System
- **Goal**: Protect photographers' intellectual property by overlaying brand logos on preview images.
- **Technical Steps**:
  - Implement an image watermarking service loading either the studio logo or custom text.
  - Render transparent watermark stamps onto the 1080px web resolution copies before writing to S3.

### 📌 Day 6: Secure Password Protection & Sharing Link Generator
- **Goal**: Secure galleries with passwords and generate cryptographically signed sharing links.
- **Technical Steps**:
  - Implement password hashing for gallery passwords.
  - Build sharing links `/public/gallery/{slug}?key=token` using hashed tokens to verify access.

### 📌 Day 7: Direct Image Downloads & Quality Formats
- **Goal**: Let clients download photos in original high-resolution, print-ready, or watermarked web formats.
- **Technical Steps**:
  - Create endpoints generating pre-signed S3 download URLs.
  - Enforce access checks, verifying gallery expiration dates and passwords before issuing URLs.

### 📌 Day 8: Parallel ZIP Packaging Pipeline
- **Goal**: Enable clients to download entire galleries as a single ZIP archive instantly.
- **Technical Steps**:
  - Build background archive workers compiling S3 streams into zip packages on the fly.
  - Stream zip outputs directly to browsers, or upload zip targets to S3 temp folders and redirect client browsers.

### 📌 Day 9: Download Tracking Audits
- **Goal**: Log download events to let photographers know who retrieved which files and when.
- **Technical Steps**:
  - Create `GalleryDownloadLog.java` storing details: IP address, file name, download type, and timestamp.
  - Compile audit logs in the photographer dashboard view.

### 📌 Day 10: E2E Gallery Delivery Integration Tests
- **Goal**: Write tests validating secure access, image conversions, and Sprint 11 DoD.
- **Technical Steps**:
  - Write MockMvc integration tests verifying:
    - Attempting to view password-protected galleries without the correct key returns unauthorized errors.
    - Watermarked preview downloads return correctly stamped file buffers.
    - Expiration dates block access dynamically.

---

## 🧪 Sprint 11 Definition of Done (DoD)
- [ ] Galleries support password parameters and expiration constraints.
- [ ] Bulk upload stores original assets securely in AWS S3 folder structures.
- [ ] Thumbnail processing automatically compiles web-optimized image sizes in the background.
- [ ] Watermark engine overlays brand logos on preview copies.
- [ ] Zip archiver packages entire galleries on the fly.
- [ ] All integration tests pass successfully (`./gradlew test`).

follow shutterflow_sprint_plan.html
