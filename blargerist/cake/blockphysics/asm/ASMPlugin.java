package blargerist.cake.blockphysics.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import squeek.asmhelper.ObfHelper;

@IFMLLoadingPlugin.MCVersion("1.8")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("blargerist.cake.blockphysics")
public class ASMPlugin implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{ClassTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		ObfHelper.setObfuscated((Boolean) data.get("runtimeDeobfuscationEnabled"));
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}