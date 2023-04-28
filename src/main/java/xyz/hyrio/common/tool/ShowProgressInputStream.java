package xyz.hyrio.common.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class ShowProgressInputStream extends InputStream {
    private final InputStream inputStream;
    private final long totalBytes;
    private final int showProcessPercentageInterval;
    private final Consumer<Integer> onProcess;

    private long readBytes = 0;
    private long nextShowProcessBytes;

    public ShowProgressInputStream(InputStream inputStream, long totalBytes, int showProcessPercentageInterval, Consumer<Integer> onProcess) {
        this.inputStream = inputStream;
        this.totalBytes = totalBytes;
        this.showProcessPercentageInterval = showProcessPercentageInterval;
        this.onProcess = onProcess;
        this.nextShowProcessBytes = showProcessPercentageInterval * totalBytes / 100;
    }

    private void showProcess() {
        if (readBytes >= nextShowProcessBytes) {
            if (onProcess != null) {
                onProcess.accept(Math.round((float) readBytes / totalBytes * 100));
            }
            nextShowProcessBytes += showProcessPercentageInterval * totalBytes / 100;
        }
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        if (read != -1) {
            readBytes++;
            showProcess();
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = inputStream.read(b, off, len);
        if (readLen != -1) {
            readBytes += readLen;
            showProcess();
        }
        return readLen;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
