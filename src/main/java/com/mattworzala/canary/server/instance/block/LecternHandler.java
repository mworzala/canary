package com.mattworzala.canary.server.instance.block;

import me.kaimu.hastebin.Hastebin;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.WrittenBookMeta;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class LecternHandler implements BlockHandler {
    private static final NamespaceID ID = NamespaceID.from("canary:lectern");

    public static final Block BLOCK = Block.LECTERN
            .withTag(Tags.Book, ItemStack.of(Material.WRITTEN_BOOK)
                    .withMeta(WrittenBookMeta.class, meta -> {
                        meta.title("CNY_Lectern");
                        meta.author("CNY_Lectern");
                        meta.pages(Component.text(""));
                    })
                    .toItemNBT())
            .withTag(Tags.Page, 1)
            .withHandler(new LecternHandler());

    private LecternHandler() {}

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        final Player player = interaction.getPlayer();
        final Block block = interaction.getBlock();

        final String testName = block.getTag(Tags.TestName);
        final String testFailure = block.getTag(Tags.TestFailure);
        final String testStacktrace = block.getTag(Tags.TestStacktrace);
        final String testHasteUrl = block.getTag(Tags.TestHasteUrl);

        Component simpleErrorPage = Component.text()
                .append(Component.text("Test Failed").color(TextColor.color(0xFF0000)).style(Style.style(TextDecoration.BOLD)))
                .append(Component.newline())
                .append(Component.text(testName).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text(testFailure))
                .build();

        TextComponent errorText;
        String hasteURL = testHasteUrl;
        try {
            if (testHasteUrl == null) {
                Hastebin hastebin = new Hastebin();
                hasteURL = hastebin.post(testStacktrace, false);
                interaction.getInstance().setBlock(interaction.getBlockPosition(), block.withTag(Tags.TestHasteUrl, hasteURL));
            }

            TextComponent urlComponent = Component.text(hasteURL)
                    .clickEvent(ClickEvent.openUrl(hasteURL));

            errorText = Component.text("Full Stack trace: ").append(urlComponent);
        } catch (IOException e) {
            e.printStackTrace();

            errorText = Component.text(testStacktrace);
        }

        player.openBook(Book.builder()
                .title(Component.text("Test Result"))
                .author(Component.text("Canary Reporter"))
                .addPage(simpleErrorPage)
                .addPage(errorText == null ? Component.text(testStacktrace) : errorText)
                .build());

        return false;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(Tags.Book, Tags.Page);
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }

    public static class Tags {
        public static final Tag<NBT> Book = Tag.NBT("Book");
        public static final Tag<Integer> Page = Tag.Integer("Page");

        public static final Tag<String> TestName = Tag.String("CNY_TestName");
        public static final Tag<String> TestFailure = Tag.String("CNY_TestFailure");
        public static final Tag<String> TestStacktrace = Tag.String("CNY_TestStacktrace");
        public static final Tag<String> TestHasteUrl = Tag.String("CNY_TestHasteUrl");
    }
}
