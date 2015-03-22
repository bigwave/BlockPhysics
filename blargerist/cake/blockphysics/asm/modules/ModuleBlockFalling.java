package blargerist.cake.blockphysics.asm.modules;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import squeek.asmhelper.ASMHelper;
import blargerist.cake.blockphysics.ModInfo;
import blargerist.cake.blockphysics.asm.IClassTransformerModule;
import blargerist.cake.blockphysics.asm.RemoveMethodAdapter;

public class ModuleBlockFalling implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		"net.minecraft.block.BlockFalling",
		"net.minecraft.world.WorldServer",
		"net.minecraft.block.Block",
		"net.minecraft.entity.item.EntityFallingBlock",
		"net.minecraft.entity.Entity"
		};
	}

	@Override
	public String getModuleName()
	{
		return "transformBlockFallingClass";
	}

	@Override
	public boolean canBeDisabled()
	{
		return false;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(bytes);
		boolean developmentEnvironment = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
		if (transformedName.equals("net.minecraft.block.BlockFalling"))
		{
			ModInfo.Log.info("Transforming class: " + transformedName);
			ClassReader cr = new ClassReader(bytes);
			ClassWriter cw = new ClassWriter(cr, 0);
			ClassVisitor cv = new RemoveMethodAdapter(ASM4, cw, "func_149674_a", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V");
			cr.accept(cv, 0);
			return cw.toByteArray();
		}
		else if (transformedName.equals("net.minecraft.world.WorldServer"))
		{ 
			ModInfo.Log.info("Transforming class: " + transformedName);
			MethodNode methodNode;

            String methodName;
            if (developmentEnvironment)
            {
                methodName = "tickUpdates";
            }
            else
            {
                methodName = "func_72955_a";
            }
            methodNode = ASMHelper.findMethodNodeOfClass(classNode, methodName, "(Z)Z");
            if (methodNode != null)
            {
                injectTickUpdatesHook(methodNode, developmentEnvironment);
            }
            else
                throw new RuntimeException("Could not find tickUpdates method in " + transformedName);
            
            if (developmentEnvironment)
            {
                methodName = "updateBlockTick";
            }
            else
            {
                methodName = "func_175654_a";
            }
           methodNode = ASMHelper.findMethodNodeOfClass(classNode,
                    methodName, 
                    "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/Block;II)V");
            if (methodNode != null)
            {
                injectScheduleBlockUpdateWithPriorityHook(methodNode, developmentEnvironment);
            }
            else
                throw new RuntimeException("Could not find scheduleBlockUpdateWithPriority method in " + transformedName);
           
            if (developmentEnvironment)
            {
                methodName = "updateBlocks";
            }
            else
            {
                methodName = "func_147456_g";
            }
		    methodNode = ASMHelper.findMethodNodeOfClass(classNode, methodName, "()V");
            if (methodNode != null)
            {
            	injectfunc_147456_gHook(methodNode, developmentEnvironment);
            }
            else
                throw new RuntimeException("Could not find updateBlocks method in " + transformedName);
             
            return ASMHelper.writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraft.block.Block"))
		{
            String methodName;
            if (developmentEnvironment)
            {
                methodName = "onNeighborBlockChange";
            }
            else
            {
                methodName = "func_176204_a";
            }
			ModInfo.Log.info("Transforming class: " + transformedName);
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, methodName , "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)V");
            if (methodNode != null)
            {
                transformOnNeighborBlockChange(methodNode, developmentEnvironment);
            }
            else
                throw new RuntimeException("Could not find onNeighborBlockChange method in " + transformedName);
            
            return ASMHelper.writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraft.entity.item.EntityFallingBlock"))
		{
            String methodName;
            if (developmentEnvironment)
            {
                methodName = "onUpdate";
            }
            else
            {
                methodName = "func_70071_h_";
            }
			ModInfo.Log.info("Transforming class: " + transformedName);
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, methodName, "()V");
            if (methodNode != null)
            {
                insertEntityFallingBlockOnUpdateHook(methodNode, developmentEnvironment);
            }
            else
                throw new RuntimeException("Could not find onUpdate method in " + transformedName);
			
            methodNode = ASMHelper.findMethodNodeOfClass(classNode, "<init>", "(Lnet/minecraft/world/World;)V");
			if (methodNode != null)
			{
				insertFrustumNoClip1(methodNode, developmentEnvironment);
			}
			else
				throw new RuntimeException("Could not find <init> method in " + transformedName);
			
			methodNode = ASMHelper.findMethodNodeOfClass(classNode,
			            "<init>", 
			            "(Lnet/minecraft/world/World;DDDLnet/minecraft/block/state/IBlockState;)V");
			if (methodNode != null)
			{
				insertFrustumNoClip2(methodNode, developmentEnvironment);
			}
			else
				throw new RuntimeException("Could not find <init> method in " + transformedName);
			
			return ASMHelper.writeClassToBytes(classNode);
		}
		else if (transformedName.equals("net.minecraft.entity.Entity"))
		{
		    String methodName;
		    if (developmentEnvironment)
		    {
		        methodName = "moveEntity";
		    }
		    else
		    {
		        methodName = "func_70091_d";
		    }
			ModInfo.Log.info("Transforming class: " + transformedName);
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, methodName, "(DDD)V");
			if (methodNode != null)
			{
				injectIfBlockFalling(methodNode, developmentEnvironment);
			}
			else
				throw new RuntimeException("Could not find moveEntity method in " + transformedName);
			
			return ASMHelper.writeClassToBytes(classNode);
		}
		return bytes;
	}

	public void verifyMethodAdded(ClassNode classNode, String name, String desc)
	{
		MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, name, desc);
		if (methodNode != null)
		{
			ModInfo.Log.info("Successfully added method: " + methodNode.name + methodNode.desc + " in " + classNode.name);
		} else
			throw new RuntimeException("Could not create method: " + name + desc + " in " + classNode.name);
	}
/*	
	public void verifyFieldAdded(ClassNode classNode, String name, String desc)
	{
		FieldNode fieldNode = ASMHelper.findFieldNodeOfClass(classNode, name, desc);
		if (fieldNode != null)
		{
			ModInfo.Log.info("Successfully added field: " + fieldNode.name + fieldNode.desc + " in " + classNode.name);
		} else
			throw new RuntimeException("Could not create field: " + name + desc + " in " + classNode.name);
	}
*/	
	public void injectTickUpdatesHook(MethodNode method, boolean developmentEnvironment)
	{
        String functionName;
        if (developmentEnvironment)
        {
            functionName = "updateTick";
        }
        else
        {
            functionName = "func_180650_b";
        }
		InsnList toFind = new InsnList();
		toFind.add(new MethodInsnNode(INVOKEVIRTUAL, 
		        "net/minecraft/block/Block",  
		        functionName, 
		        "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", false));
		AbstractInsnNode target = ASMHelper.find(method.instructions, toFind);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in WorldServer.tickUpdates");
		
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		
		// Weird?
        if (developmentEnvironment)
        {
            toInject.add(new VarInsnNode(ALOAD, 3));
        }
        else
        {
            toInject.add(new VarInsnNode(ALOAD, 4));
        }

        String fieldName;
        if (developmentEnvironment)
        {
            fieldName = "position";
        }
        else
        {
            fieldName = "field_180282_a";
        }
        
        toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/NextTickListEntry", fieldName, "Lnet/minecraft/util/BlockPos;"));
		toInject.add(new VarInsnNode(ALOAD, 0));
        if (developmentEnvironment)
        {
            fieldName = "rand";
        }
        else
        {
            fieldName = "field_73012_v";
        }
		
		toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/WorldServer", fieldName, "Ljava/util/Random;"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, 
		        "blargerist/cake/blockphysics/events/BPEventHandler", 
		        "onUpdateBlock", 
		        "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Ljava/util/Random;)V", false));
		
		method.instructions.insert(target, toInject);
	}
	
	public void injectScheduleBlockUpdateWithPriorityHook(MethodNode method, boolean developmentEnvironment)
	{ 
        String functionName;
        if (developmentEnvironment)
        {
            functionName = "updateTick";
        }
        else
        {
            functionName = "func_180650_b";
        }
		InsnList toFind = new InsnList();
		toFind.add(new MethodInsnNode(INVOKEVIRTUAL, 
		            "net/minecraft/block/Block",  
		            functionName, 
		            "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", false));
		 
		AbstractInsnNode target = ASMHelper.find(method.instructions, toFind);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in WorldServer.ScheduleBlockUpdateWithPriority");
		
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 5));
	       String fieldName;
	        if (developmentEnvironment)
	        {
	            fieldName = "position";
	        }
	        else
	        {
	            fieldName = "field_180282_a";
	        }
	        
        toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/NextTickListEntry", fieldName, "Lnet/minecraft/util/BlockPos;"));
		toInject.add(new VarInsnNode(ALOAD, 0));
        if (developmentEnvironment)
        {
            fieldName = "rand";
        }
        else
        {
            fieldName = "field_73012_v";
        }
		toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/WorldServer", fieldName, "Ljava/util/Random;"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, 
		                                "blargerist/cake/blockphysics/events/BPEventHandler",
		                                "onUpdateBlock", 
		                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Ljava/util/Random;)V",
		                                false));
		
		method.instructions.insert(target, toInject);
	}
	
	public void injectfunc_147456_gHook(MethodNode method, boolean developmentEnvironment)
	{ 
        String functionName;
        if (developmentEnvironment)
        {
            functionName = "randomTick";
        }
        else
        {
            functionName = "func_180645_a";
        }
        InsnList toFind = new InsnList();
        toFind.add(new MethodInsnNode(INVOKEVIRTUAL, 
                    "net/minecraft/block/Block",  
                    functionName, 
                    "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", false));
         
        AbstractInsnNode target = ASMHelper.find(method.instructions, toFind);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in WorldServer.func_147456_g");
		
		InsnList toInject = new InsnList();
        toInject.add(new VarInsnNode(ALOAD, 0));
        toInject.add(new VarInsnNode(ALOAD, 19));
		
/*
		toInject.add(new VarInsnNode(ILOAD, 16));
		toInject.add(new VarInsnNode(ILOAD, 5));
		toInject.add(new InsnNode(IADD));
		toInject.add(new VarInsnNode(ILOAD, 18));
		toInject.add(new VarInsnNode(ALOAD, 11));
		toInject.add(new MethodInsnNode(INVOKEVIRTUAL, 
		                                "net/minecraft/world/chunk/storage/ExtendedBlockStorage", 
		                                "getYLocation",
		                                "()I", 
		                                false));
		toInject.add(new InsnNode(IADD));
		toInject.add(new VarInsnNode(ILOAD, 17));
		toInject.add(new VarInsnNode(ILOAD, 6));
		toInject.add(new InsnNode(IADD));
*/
		toInject.add(new VarInsnNode(ALOAD, 0));
		String fieldName;
        if (developmentEnvironment)
        {
            fieldName = "rand";
        }
        else
        {
            fieldName = "field_73012_v";
        }
        toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/WorldServer", fieldName, "Ljava/util/Random;"));
		toInject.add(new MethodInsnNode(INVOKESTATIC, 
		        "blargerist/cake/blockphysics/events/BPEventHandler", 
		        "onUpdateBlock",
		        "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Ljava/util/Random;)V", false));
		
		method.instructions.insert(target, toInject);
	}
	
	public void transformOnNeighborBlockChange(MethodNode method, boolean developmentEnvironment)
	{
	    AbstractInsnNode target = ASMHelper.findLastInstructionWithOpcode(method, RETURN);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in Block.onNeighborBlockChange");
		
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, 2));
		//toInject.add(new VarInsnNode(ALOAD, 3));
		toInject.add(new VarInsnNode(ALOAD, 4));
		//toInject.add(new VarInsnNode(ALOAD, 5));
		toInject.add(new MethodInsnNode(INVOKESTATIC, 
		                "blargerist/cake/blockphysics/events/BPEventHandler", 
		                "onNeighborBlockChange",
		                "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/Block;)V", false));
		
		method.instructions.insertBefore(target, toInject);
		
	}
	
	public void insertEntityFallingBlockOnUpdateHook(MethodNode method, boolean developmentEnvironment)
	{
		AbstractInsnNode target = ASMHelper.findFirstInstruction(method);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in EntityFallingBlock.onUpdate");
		
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new MethodInsnNode(INVOKESTATIC, 
		        "blargerist/cake/blockphysics/util/EntityMove",
		        "onUpdate",
		        "(Lnet/minecraft/entity/item/EntityFallingBlock;)V", false));
		toInject.add(new InsnNode(RETURN));
		
		method.instructions.insertBefore(target, toInject);
	}

	public void insertFrustumNoClip1(MethodNode method, boolean developmentEnvironment)
	{
		AbstractInsnNode target = ASMHelper.findLastInstructionWithOpcode(method, RETURN);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in EntityFallingBlock.<init>(World)");
		
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
        String fieldName;
        if (developmentEnvironment)
        {
            fieldName = "ignoreFrustumCheck";
        }
        else
        {
            fieldName = "field_70158_ak";
        }
		toInject.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/item/EntityFallingBlock", fieldName, "Z"));
		//toInject.add(new VarInsnNode(ALOAD, 0));
		//toInject.add(new InsnNode(ICONST_1));
		//toInject.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/item/EntityFallingBlock", "field_70145_X", "Z"));
		
		method.instructions.insertBefore(target, toInject);
	}
	
	public void insertFrustumNoClip2(MethodNode method, boolean developmentEnvironment)
	{
		AbstractInsnNode target = ASMHelper.findLastInstructionWithOpcode(method, RETURN);
		
		if (target == null)
			throw new RuntimeException("Unexpected instruction pattern in EntityFallingBlock.<init>(World;DDDBlock;I)");
		
		InsnList toInject = new InsnList();
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_0));
        String fieldName;
        if (developmentEnvironment)
        {
            fieldName = "ignoreFrustumCheck";
        }
        else
        {
            fieldName = "field_70158_ak";
        }
		toInject.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/item/EntityFallingBlock", fieldName, "Z"));
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new InsnNode(ICONST_1));
        if (developmentEnvironment)
        {
            fieldName = "noClip";
        }
        else
        {
            fieldName = "field_70145_X";
        }
		toInject.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/item/EntityFallingBlock", fieldName, "Z"));
		
		method.instructions.insertBefore(target, toInject);
	}
	
	public void injectIfBlockFalling(MethodNode method, boolean developmentEnvironment)
	{
		InsnList toFind = new InsnList();
		toFind.add(new VarInsnNode(ALOAD, 0));
		String functionName;
		if (developmentEnvironment)
		{
		    functionName = "doBlockCollisions";
		}
		else
		{
		    functionName = "func_145775_I";
		}
		
		toFind.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/Entity", functionName, "()V", false));
		
		AbstractInsnNode start = ASMHelper.find(method.instructions, toFind);
		AbstractInsnNode end = ASMHelper.move(start, 1);
		
		if (start == null || end == null)
			throw new RuntimeException("Unexpected instructions pattern in Entity.moveEntity");
		
		InsnList firstInject = new InsnList();
		firstInject.add(new VarInsnNode(ALOAD, 0));
		firstInject.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/entity/item/EntityFallingBlock"));
		LabelNode label1 = new LabelNode();
		firstInject.add(new JumpInsnNode(IFEQ, label1));
		firstInject.add(new VarInsnNode(ALOAD, 0));
		firstInject.add(new MethodInsnNode(INVOKESTATIC, 
		        "blargerist/cake/blockphysics/util/EntityMove",
		        "checkEntityBlockCollisions", 
		        "(Lnet/minecraft/entity/Entity;)V",   
		        false));
		LabelNode label2 = new LabelNode();
		firstInject.add(new JumpInsnNode(GOTO, label2));
		firstInject.add(label1);

		InsnList secondInject = new InsnList();
		secondInject.add(label2);
		
		method.instructions.insertBefore(start, firstInject);
		method.instructions.insert(end, secondInject);
	}
}