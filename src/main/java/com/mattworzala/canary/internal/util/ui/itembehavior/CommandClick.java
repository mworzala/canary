package com.mattworzala.canary.internal.util.ui.itembehavior;

import com.mattworzala.canary.internal.util.ui.itembehavior.argument.Argument;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandClick {

    String baseCommands;
    List<Argument> args;

    public CommandClick(String baseCommands, List<Argument> args) {
        this.baseCommands = baseCommands;
        this.args = args;
    }

    public String handle(Player player, Point p) throws Exception {
        List<String> arguments = new ArrayList<>();
        for (Argument arg : args) {
            arguments.add(arg.get(player, p).get());
        }

        String joinedArgs = String.join(" ", arguments);
        return baseCommands + " " + joinedArgs;
    }
}
