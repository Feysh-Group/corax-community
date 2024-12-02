## 存档提取期间的任意文件访问（“Zip Slip”）

如果未正确验证存档中的文件名，则从恶意 zip 文件或类似类型的存档中提取文件将面临目录遍历攻击的风险。

Zip 存档包含代表存档中每个文件的存档条目。这些条目包括该条目的文件路径，但这些文件路径不受限制，并且可能包含意外的特殊元素，例如目录遍历元素（..）。如果这些文件路径用于创建文件系统路径，则文件操作可能会发生在意外的位置。这可能会导致敏感信息被泄露或删除，或者攻击者能够通过修改意外文件来影响行为。

例如，如果 zip 文件包含文件条目 ..\sneaky-file，并且 zip 文件被提取到目录 c:\output，直接组合路径将导致文件被写入c:\sneaky-file。

## 例子
在此示例中，从 zip 存档项条目获取的文件路径与目标目录组合。结果将用作目标文件路径，而不验证结果是否位于目标目录内。如果提供的 zip 文件包含诸如 ..\sneaky-file 之类的存档路径，则该文件将写入目标目录之外。
```java
void writeZipEntry(ZipEntry entry, File destinationDir) {
    File file = new File(destinationDir, entry.getName());
    FileOutputStream fos = new FileOutputStream(file); // BAD
    // ... write entry to fos ...
}
```

为了修复这个漏洞，我们需要验证规范化后的 file 是否仍然以 destinationDir 作为前缀，如果不是，则抛出异常。
```java
void writeZipEntry(ZipEntry entry, File destinationDir) {
    File file = new File(destinationDir, entry.getName());
    if (!file.toPath().normalize().startsWith(destinationDir.toPath()))
        throw new Exception("Bad zip entry");
    FileOutputStream fos = new FileOutputStream(file); // OK
    // ... write entry to fos ...
}
```
