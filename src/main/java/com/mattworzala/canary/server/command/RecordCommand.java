package com.mattworzala.canary.server.command;

import com.mattworzala.canary.server.recording.PacketRecorder;
import com.mattworzala.canary.server.recording.PacketRecording;
import com.mattworzala.canary.server.recording.PlaybackPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.utils.time.TimeUnit;

public class RecordCommand extends Command {
    private PacketRecorder recorder = null;
    private PacketRecording recording = null;
    private PlaybackPlayer playback = null;

    public RecordCommand() {
        super("record", "rec");

        addSyntax(this::onStart, ArgumentType.Literal("start"));
        addSyntax(this::onStop, ArgumentType.Literal("stop"));
        addSyntax(this::onPlay, ArgumentType.Literal("play"));

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
}
