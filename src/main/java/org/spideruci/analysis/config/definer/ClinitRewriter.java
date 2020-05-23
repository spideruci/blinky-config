package org.spideruci.analysis.config.definer;

import static org.spideruci.analysis.config.definer.ConfigFieldsDefiner.toPrimitiveType;
import static org.spideruci.analysis.config.utils.SystemProperties.has;
import static org.spideruci.analysis.config.utils.SystemProperties.valueFor;

import java.util.ArrayList;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class ClinitRewriter extends MethodVisitor {

  private static final String CONFIG_PROFILER_CLASSNAME = "config.profilerclassname";

  private Map<String, ?> config;
  private String className;
  private String profilerClassName;
  
  public ClinitRewriter(MethodVisitor mv, String className, Map<String, ?> config) {
    super(Opcodes.ASM5, mv);
    this.className = className;
    this.config = config;
    if (has(CONFIG_PROFILER_CLASSNAME)) {
      String profilerClassName = valueFor(CONFIG_PROFILER_CLASSNAME);
      this.profilerClassName =  profilerClassName.replace(".", "/");
    } else {
      this.profilerClassName = null;
    }

  }
  
  @Override
  public void visitInsn(int opcode) {
    this.visitReturnInsn(opcode);
    super.visitInsn(opcode);
  }

  private void visitReturnInsn(int opcode) {
    if(opcode != Opcodes.RETURN) {
      return;
    }

    this.assignProfilerField(this.profilerClassName);

    for(String fieldName : config.keySet()) {
      Object fieldValue = config.get(fieldName);
      Class<?> fieldClass = fieldValue.getClass();
      Type fieldType = toPrimitiveType(fieldClass);

      switch(fieldType.getSort()) {
        case Type.INT: case Type.BOOLEAN: case Type.FLOAT: case Type.DOUBLE:
          break;

        case Type.OBJECT:
          if(fieldValue.getClass() != String.class) {
            throw new RuntimeException("Unhandled type: " + fieldClass.getName());
          }
          break;

        case Type.ARRAY:
          @SuppressWarnings("unchecked")
          ArrayList<String> list = (ArrayList<String>)fieldValue;
          assignField(list, fieldName);
          break;

        default:
          throw new RuntimeException("Unhandled type: " + fieldClass.getName());
      }
    }
  }
  
  @Override
  public void visitEnd() {
    super.visitEnd();
  }
  
  @SuppressWarnings("unused")
  private void assignField(int value, String fieldName) {
    mv.visitLdcInsn(value);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, "I");
  }
  
  @SuppressWarnings("unused")
  private void assignField(double value, String fieldName) {
    assignField((float) value, fieldName);
  }
  
  private void assignField(float value, String fieldName) {
    mv.visitLdcInsn(value);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, "F");
  }
  
  @SuppressWarnings("unused")
  private void assignField(boolean value, String fieldName) {
    mv.visitLdcInsn(value);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, "Z");
  }
  
  @SuppressWarnings("unused")
  private void assignField(String value, String fieldName) {
    mv.visitLdcInsn(value);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, stringDesc);
  }
  
  private void assignField(ArrayList<String> value, String fieldName) {
    final int arraysize = value.size();
    
    mv.visitLdcInsn(arraysize);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
    
    for(int index = 0; index < arraysize; index += 1) {
      mv.visitInsn(Opcodes.DUP);
      mv.visitLdcInsn(index);
      String arrayitem = value.get(index);
      mv.visitLdcInsn(arrayitem);
      mv.visitInsn(Opcodes.AASTORE);
    }
    
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, stringArrayDesc);
  }

  private void assignProfilerField(String objectTypeInternalName) {
    if(objectTypeInternalName == null) {
      return;
    }

//    4: new           #77                 // class org/spideruci/analysis/dynamic/api/EmptyProfiler
//    7: dup
//    8: invokespecial #78                 // Method org/spideruci/analysis/dynamic/api/EmptyProfiler."<init>":()V
//    11: putstatic     #80                 // Field profiler:Lorg/spideruci/analysis/dynamic/api/IProfiler;


    mv.visitTypeInsn(Opcodes.NEW, objectTypeInternalName);
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objectTypeInternalName, "<init>", "()V", false);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, "profiler", "Lorg/spideruci/analysis/dynamic/api/IProfiler;");
  }

  private static final String stringArrayDesc = "[Ljava/lang/String;";
  private static final String stringDesc = "Ljava/lang/String;";
}
