![SuperMartijn642's Config Lib](https://imgur.com/oVcLlAO.png)   
**SuperMartijn642's Config Lib** allows you to specify a config once and it then handles reloading values between world loads, syncing values with clients, and generating values for client- or server-only on its own.

---

### CurseForge
For more info and downloads, check out the project on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/supermartijn642s-config-lib)

---

### Creating a config:
A config is created using a `ModConfigBuilder`.
Simply create a new instance using `#ModConfigBuilder(String)` passing in your modid.
```java
   ModConfigBuilder builder = new ModConfigBuilder();
```
A value can be added to the config with `ModConfigBuilder#define` which takes a name and a default value.
For integer and double values a minimum and maximum value are also required.
`ModConfigBuilder#define` returns a `Supplier` which should be stored to retrieve the value from the config.
```java
   Supplier<Boolean> booleanValue = builder.define( "booleanValue", true );

   Supplier<Integer> integerValue = builder.define( "integerValue", 5, 0, 10 );

   Supplier<Double> doubleValue = builder.define( "doubleValue", 0.5, 0, 1);

   Supplier<ExampleEnum> enumValue = builder.define( "enumValue", ExampleEnum.VALUE_1 );
```
A comment can be added to a value by calling `ModConfigBuilder#comment(String)` before defining the value.
```java
   Supplier<Boolean> valueWithComment = builder.comment( "this is a comment for 'valueWithComment'" ).define( "valueWithComment ", true );
```
By default values are reloaded when world is loaded.
This can be changed to only reload a value when Minecraft launches by calling `ModConfigBuilder#gameRestart()` before defining the value.
```java
   Supplier<Boolean> notReloadedValue = builder.comment( "this is value will not be reloaded" ).define( "notReloadedValue", true );
```
Values in COMMON or SERVER configs are synchronized with clients by default, to prevent this use `ModConfigBuilder#dontSync()`.
```java
   Supplier<Boolean> notSynchronizedValue = builder.comment( "this is value will not be synchronized" ).define( "notSynchronizedValue", true );
```
Values can also be put into categories.
`ModConfigBuilder#push(String)` pushes a category and `ModConfigBuilder#pop()` pops a category.
```java
   builder.push( "special" );
   
   Supplier<Boolean> specialValue = builder.comment( "this value is in the 'special' category" ).define( "specialValue", true );
   
   builder.pop();
```
A comment can be added to the active category using `ModConfigBuilder#categoryComment(String)`.
```java
   builder.push( "client" ).categoryComment( "this, is a comment for the 'client' category" );
```
After defining all values `ModConfigBuilder#build()` must be called to finish the config.
```java
   builder.build();
```
Now the values in your config will reloaded and synced automatically and the values can be retrieved using the stored `Supplier` instances.
This will work for all available versions, that includes Minecraft 1.12, 1.14, 1.15, and 1.16

---

### Example Mod:
For a concrete example of how to use Config Lib checkout [the example mod](https://github.com/SuperMartijn642/SuperMartijn642sConfigLib/blob/1.16/src/test/java/ExampleModConfig.java).

---

### FAQ
Can I use your mod in my modpack?  
Yes, feel free to use my mod in your modpack

---

### Discord
For future content, upcoming mods, and discussion, feel free to join the SuperMartijn642 discord server!  
[<img width='200' src='https://snrclan.com/wp-content/uploads/2020/02/join-discord-png-13.png'>](https://discord.gg/QEbGyUYB2e)
