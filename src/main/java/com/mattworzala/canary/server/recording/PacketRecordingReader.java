package com.mattworzala.canary.server.recording;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface PacketRecordingReader extends AutoCloseable {

    @NotNull
    PacketRecording read() throws IOException;
}
