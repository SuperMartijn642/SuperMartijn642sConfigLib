import net.minecraftforge.fml.common.Mod;

/**
 * Created 1/21/2021 by SuperMartijn642
 */
@Mod(modid = ConfigLibExampleMod.MODID, name = ConfigLibExampleMod.NAME, version = ConfigLibExampleMod.VERSION, dependencies = ConfigLibExampleMod.DEPENDENCIES)
public class ConfigLibExampleMod {

    public static final String MODID = "configlibexamplemod";
    public static final String NAME = "SuperMartijn642's Config Lib Example Mod";
    public static final String VERSION = "1.0.0";
    public static final String DEPENDENCIES = "required-after:supermartijn642configlib@[1.0.0,)";

    public ConfigLibExampleMod(){
    }

}
