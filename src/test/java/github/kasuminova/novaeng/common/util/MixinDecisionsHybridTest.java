package github.kasuminova.novaeng.common.util;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinDecisionsHybridTest {
    private static final String DECISIONS = "github.kasuminova.novaeng.common.util.MixinDecisions";
    private static final String DISCOVERER = "com.cleanroommc.discovery.CleanroomModDiscoverer";

    @Test
    void ordinaryEnvironmentKeepsLazySections() throws Exception {
        assertDecision(null, true);
    }

    @Test
    void catRoomDisablesOnlyLazySections() throws Exception {
        assertDecision("catroom/CatRoom.class", false);
    }

    @Test
    void catServerDisablesOnlyLazySections() throws Exception {
        assertDecision("catserver/server/CatServer.class", false);
    }

    private static void assertDecision(String marker, boolean expected) throws Exception {
        ClassLoader parent = MixinDecisionsHybridTest.class.getClassLoader();
        ClassLoader isolated = new ClassLoader(parent) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!name.equals(DECISIONS) && !name.equals(DISCOVERER)) {
                    return super.loadClass(name, resolve);
                }
                Class<?> found = findLoadedClass(name);
                if (found == null) {
                    byte[] bytes;
                    if (name.equals(DISCOVERER)) {
                        bytes = discoveryStub();
                    } else {
                        try (var input = parent.getResourceAsStream(name.replace('.', '/') + ".class")) {
                            if (input == null) throw new ClassNotFoundException(name);
                            bytes = input.readAllBytes();
                        } catch (IOException e) {
                            throw new ClassNotFoundException(name, e);
                        }
                    }
                    found = defineClass(name, bytes, 0, bytes.length);
                }
                if (resolve) resolveClass(found);
                return found;
            }

            @Override
            public URL getResource(String name) {
                if (name.equals("catroom/CatRoom.class") || name.equals("catserver/server/CatServer.class")) {
                    return name.equals(marker) ? parent.getResource(DECISIONS.replace('.', '/') + ".class") : null;
                }
                return super.getResource(name);
            }
        };
        var method = isolated.loadClass(DECISIONS).getMethod("shouldApply", String.class);
        boolean actual = (boolean) method.invoke(null, "minecraft.MixinChunkLazyEntityLists");
        if (expected) assertTrue(actual); else assertFalse(actual);
        assertTrue((boolean) method.invoke(null, "minecraft.forge.MixinOreRecipeRevision"));
        assertTrue((boolean) method.invoke(null, "minecraft.UnrelatedMixin"));
    }

    // Avoid bootstrapping Forge discovery; all optional mods are absent in this fixture.
    private static byte[] discoveryStub() {
        String owner = DISCOVERER.replace('.', '/');
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null);
        var ctor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(1, 1);
        ctor.visitEnd();
        var instance = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "instance", "()L" + owner + ";", null, null);
        instance.visitCode();
        instance.visitTypeInsn(Opcodes.NEW, owner);
        instance.visitInsn(Opcodes.DUP);
        instance.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, "<init>", "()V", false);
        instance.visitInsn(Opcodes.ARETURN);
        instance.visitMaxs(2, 0);
        instance.visitEnd();
        var present = writer.visitMethod(Opcodes.ACC_PUBLIC, "isModPresent", "(Ljava/lang/String;)Z", null, null);
        present.visitCode();
        present.visitInsn(Opcodes.ICONST_0);
        present.visitInsn(Opcodes.IRETURN);
        present.visitMaxs(1, 2);
        present.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}