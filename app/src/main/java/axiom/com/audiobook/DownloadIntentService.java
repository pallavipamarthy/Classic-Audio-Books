package axiom.com.audiobook;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.BookContract.BookEntry;
import axiom.com.audiobook.data.Chapter;
import axiom.com.audiobook.data.ChapterContract.ChapterEntry;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.JsonUtils;
import axiom.com.audiobook.data.NetworkUtils;

public class DownloadIntentService extends IntentService {
    Book mCurrentBook;

    public DownloadIntentService() {
        super(DownloadIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mCurrentBook = intent.getParcelableExtra("book_obj");
        String absolutePath = "";
        String fileName = "";
        try {
            int index = mCurrentBook.getZipFileUrlString().lastIndexOf("/");
            fileName = mCurrentBook.getZipFileUrlString().substring(index + 1);
            absolutePath = downloadFile(mCurrentBook.getZipFileUrlString(), fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String unzipFolder = unzipFile(absolutePath, fileName);
        addBook(unzipFolder);
        addChapters();
        sendDownloadCompleteIntent();
    }

    public String downloadFile(String downloadUrl, String fileName) throws IOException {
        URL url = new URL(downloadUrl);
        File file = new File(getFilesDir(), fileName);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            int file_size = urlConnection.getContentLength();
            FileOutputStream fos = new FileOutputStream(file);

            int bytesRead = -1;
            double totalBytesRead = 0;
            int percent = 0;
            int reportedPercent = -1;
            byte[] buffer = new byte[1024 * 5];
            while ((bytesRead = in.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                percent = (int) (((totalBytesRead) / (double) file_size) * 100.0);
                if (percent > 0 && percent > reportedPercent) {
                    sendProgress(percent);
                    reportedPercent = percent;
                }
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return file.getAbsolutePath();
    }

    private void sendProgress(int progress) {
        Intent intent = new Intent(Constants.ACTION_DOWNLOAD_PROGRESS);
        intent.putExtra(getString(R.string.progress_extra_text), progress);
        intent.putExtra(Constants.EXTRA_BOOK_ID,mCurrentBook.getBookId());
        sendBroadcast(intent);
    }

    private String unzipFile(String zipFilePath, String zipFileName) {
        // Create folder for the unzip contents using Book ID
        String unzipFolderPath = getFilesDir() + "/" + mCurrentBook.getBookId();
        File unzipFolderDir = new File(unzipFolderPath);
        if (!unzipFolderDir.exists()) {
            unzipFolderDir.mkdir();
        }

        byte[] buffer = new byte[1024];
        ZipInputStream inputStream;
        try {
            inputStream = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = inputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(unzipFolderDir + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = inputStream.getNextEntry();
            }
            inputStream.closeEntry();
            inputStream.close();
            File file = new File(getFilesDir(), zipFileName);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unzipFolderDir.getAbsolutePath();
    }

    private void addBook(String absolutePath) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookEntry.COLUMN_BOOK_IMAGE_URL, mCurrentBook.getImageUrl());
        contentValues.put(BookEntry.COLUMN_BOOK_THUMBNAIL, mCurrentBook.getThumbNailUrl());
        contentValues.put(BookEntry.COLUMN_BOOK_ID, mCurrentBook.getBookId());
        contentValues.put(BookEntry.COLUMN_BOOK_TITLE, mCurrentBook.getTitle());
        contentValues.put(BookEntry.COLUMN_BOOK_DESC, mCurrentBook.getDescription());
        contentValues.put(BookEntry.COLUMN_BOOK_ABSOLUTE_PATH, absolutePath);
        contentValues.put(BookEntry.COLUMN_BOOK_AUTHOR, mCurrentBook.getAuthorName());
        contentValues.put(BookEntry.COLUMN_BOOK_YEAR, mCurrentBook.getCopyrightYear());
        contentValues.put(BookEntry.COLUMN_BOOK_DURATION, mCurrentBook.getTotalTime());
        getContentResolver().insert(BookEntry.CONTENT_URI, contentValues);
    }

    private void addChapters() {
        String id = mCurrentBook.getBookId();
        String url = Constants.BOOK_QUERY + Constants.BOOKD_ID + id + Constants.EXTENDED_QUERY_END_STRING;
        List<Chapter> chapterList = new ArrayList<>();
        URL createdUrl = NetworkUtils.createUrl(url);
        String jsonResponse = "";
        try {
            jsonResponse = NetworkUtils.getResponseFromHttpUrl(createdUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        chapterList = JsonUtils.extractChaptersFromJson(jsonResponse);
        for (int i = 0; i < chapterList.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChapterEntry.COLUMN_BOOK_ID, chapterList.get(i).getBookId());
            contentValues.put(ChapterEntry.COLUMN_CHAPTER_ID, chapterList.get(i).getChapterId());
            contentValues.put(ChapterEntry.COLUMN_CHAPTER_TITLE, chapterList.get(i).getChapterTitle());
            contentValues.put(ChapterEntry.COLUMN_PLAYTIME, chapterList.get(i).getPlayTime());
            getContentResolver().insert(ChapterEntry.CONTENT_URI, contentValues);
        }
    }

    private void sendDownloadCompleteIntent() {
        Intent downloadCompletedIntent = new Intent(Constants.ACTION_DOWNLOAD_COMPLETED);
        sendBroadcast(downloadCompletedIntent);
    }
}

