package com.mattworzala.canary.internal.server.instance.block;

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

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

        TextComponent errorText;
        String hasteURL = testHasteUrl;
        try {
            if (testHasteUrl == null) {
                hasteURL = postToHastebin(testStacktrace, false);
                interaction.getInstance().setBlock(interaction.getBlockPosition(), block.withTag(Tags.TestHasteUrl, hasteURL));
            }

            TextComponent urlComponent = Component.text(hasteURL)
                    .clickEvent(ClickEvent.openUrl(hasteURL));

            errorText = Component.text("View on Hastebin: ").append(urlComponent);
        } catch (IOException e) {
            e.printStackTrace();

            errorText = Component.text(testStacktrace);
        }

        Component simpleErrorPage = Component.text()
                .append(Component.text("Test Failed").color(TextColor.color(0xFF0000)).style(Style.style(TextDecoration.BOLD)))
                .append(Component.newline())
                .append(Component.text(testName).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text(testFailure))
                .append(Component.newline())
                .append(Component.newline())
                .append(errorText)
                .build();

        player.openBook(Book.builder()
                .title(Component.text("Test Result"))
                .author(Component.text("Canary Reporter"))
                .addPage(simpleErrorPage)
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

    public static String postToHastebin(String text, boolean raw) throws IOException {
        byte[] postData = text.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        String requestURL = "https://hastebin.com/documents";
        URL url = new URL(requestURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Hastebin Java Api");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);

        String response = null;
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.contains("\"key\"")) {
            response = response.substring(response.indexOf(":") + 2, response.length() - 2);

            String postURL = raw ? "https://hastebin.com/raw/" : "https://hastebin.com/";
            response = postURL + response;
        }

        return response;
    }
}
