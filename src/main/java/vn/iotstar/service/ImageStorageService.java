package vn.iotstar.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.exception.ImageStorageException;

@Service
public class ImageStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path rootDirectory;

    public ImageStorageService(@Value("${app.upload-dir:E:/upload}") String uploadDirectory) {
        rootDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDirectory.resolve("category"));
            Files.createDirectories(rootDirectory.resolve("product"));
        } catch (IOException ex) {
            throw new ImageStorageException("Không thể tạo thư mục lưu ảnh tại " + rootDirectory + ".", ex);
        }
    }

    public String store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) return null;
        validateFolder(folder);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ImageStorageException("File được chọn không phải là ảnh.");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        extension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageStorageException("Ảnh phải có định dạng JPG, JPEG, PNG, GIF hoặc WEBP.");
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path folderPath = rootDirectory.resolve(folder).normalize();
        Path destination = folderPath.resolve(filename).normalize();
        if (!destination.startsWith(folderPath)) {
            throw new ImageStorageException("Đường dẫn lưu ảnh không hợp lệ.");
        }

        try (InputStream input = file.getInputStream()) {
            Files.createDirectories(folderPath);
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            return folder + "/" + filename;
        } catch (IOException ex) {
            throw new ImageStorageException("Không thể lưu ảnh vào " + folderPath + ".", ex);
        }
    }

    public void delete(String relativePath, String folder) {
        if (relativePath == null || relativePath.isBlank()) return;
        validateFolder(folder);

        String normalizedValue = relativePath.replace('\\', '/').replaceFirst("^/+", "");
        Path folderPath = rootDirectory.resolve(folder).normalize();
        Path filePath = rootDirectory.resolve(normalizedValue).normalize();
        if (!filePath.startsWith(folderPath)) return;

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
        }
    }

    private void validateFolder(String folder) {
        if (!folder.equals("category") && !folder.equals("product")) {
            throw new ImageStorageException("Thư mục ảnh không hợp lệ.");
        }
    }
}
