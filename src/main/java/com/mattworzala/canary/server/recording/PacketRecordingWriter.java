package com.mattworzala.canary.server.recording;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface PacketRecordingWriter extends AutoCloseable {

    void write(@NotNull PacketRecording recording) throws IOException;
}
