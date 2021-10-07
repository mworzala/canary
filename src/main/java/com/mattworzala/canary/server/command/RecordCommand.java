package com.mattworzala.canary.server.command;

import com.mattworzala.canary.server.recording.PacketRecorder;
import com.mattworzala.canary.server.recording.PacketRecording;
import com.mattworzala.canary.server.recording.PlaybackPlayer;
import com.mattworzala.canary.server.recording.cpr.CprReader;
import com.mattworzala.canary.server.recording.cpr.CprWriter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.utils.time.TimeUnit;

import java.io.IOException;
import java.nio.file.Paths;

public class RecordCommand extends Command {
    private PacketRecorder recorder = null;
    private PacketRecording recording = null;
    private PlaybackPlayer playback = null;

    public RecordCommand() {
        super("record", "rec");

        addSyntax(this::onStart, ArgumentType.Literal("start"));
        addSyntax(this::onStop, ArgumentType.Literal("stop"));
        addSyntax(this::onPlay, ArgumentType.Literal("play"));

        addSyntax(this::onSave, ArgumentType.Literal("save"));
        addSyntax(this::onLoad, ArgumentType.Literal("load"));
    }

    private void onStart(CommandSender sender, CommandContext context) {
        if (!sender.isPlayer()) return;

        sender.sendMessage("Starting recording");
        recorder = new PacketRecorder(sender.asPlayer());
        recorder.start();
    }

    private void onStop(CommandSender sender, CommandContext context) {
        if (recorder == null) return;

        recording = recorder.stop();
        recorder = null;
        sender.sendMessage("Stopped recording.");
    }

    private void onPlay(CommandSender sender, CommandContext context) {
        if (recording == null) return;

        sender.sendMessage("Playing");
        playback = new PlaybackPlayer(recording);
        MinecraftServer.getSchedulerManager().buildTask(() -> playback.start())
                .delay(1, TimeUnit.SECOND)
                .schedule();
    }

    private void onSave(CommandSender sender, CommandContext context) {
        if (recording == null) return;

        try (var writer = new CprWriter(Paths.get("./out.cpr"))) {
            writer.write(recording);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void onLoad(CommandSender sender, CommandContext context) {
        try (var reader = new CprReader(Paths.get("./out.cpr"))) {
            recording = reader.read();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
