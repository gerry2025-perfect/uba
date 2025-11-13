package com.iwhalecloud.bss.uba.common.javaassist.config;

public class Path
{
  private String packageName;
  private String className;
  private String methodName;
  private PathType type;

  public String getPackageName() { return this.packageName; }

  public void setPackageName(String packageName) { this.packageName = packageName; }

  public String getClassName() { return this.className; }

  public void setClassName(String className) { this.className = className; }

  public String getMethodName() { return this.methodName; }

  public void setMethodName(String methodName) { this.methodName = methodName; }

  public PathType getType() { return this.type; }

  public void setType(PathType type) { this.type = type; }

  public static Path build(String path) {
    Path p = new Path();
    int start = path.indexOf("#");
    if (start != -1) {
      String pkg = path.substring(0, start);
      String clazz = path.substring(start + 1);
      p.setPackageName(pkg);
      int end = clazz.indexOf(".");
      if (end != -1) {
        p.setClassName(clazz.substring(0, end));
        p.setMethodName(clazz.substring(end + 1));
        p.setType(PathType.METHOD);
      } else {
        p.setClassName(clazz);
        p.setType(PathType.CLASS);
      }
    } else {

      p.setPackageName(path);
      p.setType(PathType.PACKAGE);
    }
    return p;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Path path = (Path)o;
    if ((this.className != null) ? !this.className.equals(path.className) : (path.className != null)) return false;
    if ((this.methodName != null) ? !this.methodName.equals(path.methodName) : (path.methodName != null)) return false;
    if (!this.packageName.equals(path.packageName)) return false;
    if (this.type != path.type) return false;
    return true;
  }


  public int hashCode() {
    int result = this.packageName.hashCode();
    result = 31 * result + ((this.className != null) ? this.className.hashCode() : 0);
    result = 31 * result + ((this.methodName != null) ? this.methodName.hashCode() : 0);
    return 31 * result + this.type.hashCode();
  }


  public String getFullClassName() {
    StringBuilder buff = new StringBuilder(this.packageName);
    buff.append(".").append(this.className);
    return buff.toString();
  }


  public String toString() {
    StringBuilder buff = new StringBuilder(this.packageName);
    if (this.className != null) {
      buff.append("#").append(this.className);
    }
    if (this.methodName != null) {
      buff.append(".").append(this.methodName);
    }
    return buff.toString();
  }
}