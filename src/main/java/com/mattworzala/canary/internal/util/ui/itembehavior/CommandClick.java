package com.mattworzala.canary.internal.util.ui.itembehavior;

import com.mattworzala.canary.internal.util.ui.itembehavior.argument.Argument;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CommandClick {

    String baseCommands;
    List<Argument> args;

    public CommandClick(String baseCommands, List<Argument> args) {
        this.baseCommands = baseCommands;
        this.args = args;
    }

    public CompletableFuture<String> handle(Player player, Point p) throws Exception {
        CompletableFuture<String>[] futures;
        futures = new CompletableFuture[args.size()];
        int indx = 0;
        for (Argument arg : args) {
            futures[indx] = arg.get(player, p);
            indx++;
        }
        return CompletableFuture.allOf(futures)
                .thenApply(new Function<Void, String>() {
                    @Override
                    public String apply(Void unused) {
                        List<String> results = new ArrayList<>(args.size());
                        for (CompletableFuture<String> f : futures) {
                            try {
                                results.add(f.get());
                            } catch (Exception e) {
                                return (String) null;
                            }
                        }
                        String joinedArgs = String.join(" ", results);
                        System.out.println("returning \"" + baseCommands + " " + joinedArgs + "\"");
                        return baseCommands + " " + joinedArgs;
                    }
                });
    }

}
