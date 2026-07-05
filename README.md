# 📖 About
Adds various commands and other utilities. Datapackers and mapmakers may find this useful.

# 👀 Main Features

- Gives access to modify the player using /data (player data).

## Commands
- `/motion <add/set> <entity> <momentum>` - Applies the given momentum to the entity.
- `/gui <open/close> <player> <menu> <name> <force>`
    1. `close` - Closes a menu for the player. You can optionally specify the type of menu that should be closed.
    2. `open` - Opens a menu for the player. You can optionally specify the name of the container and whether it will open on top of other menus (false by default).
- `/eval <expression> <scale> <arguments/with>` - Evaluates code represented as a string and returns its completion value. You can use `arguments` and `with` for variables in an expression. For dynamic variables, you can use a macro or `with`.
- `/provoke <victim> <provoker>` - Provokes `victim` against `provoker`. If `victim` is a passive entity, it will panic instead
- `/findblock <from> <to> <block> store <block/entity/storage>` - Searches for blocks within a specified cuboid. If the `store` argument is not provided, returns 1 or 0 depending on whether the command succeeded or failed. If the `store` argument is provided, stores all block IDs and positions in the specified destination, returns the total count of blocks found.

**Full functionality is described on the [wiki](https://github.com/JustJabka/Tophat/wiki)** 💡