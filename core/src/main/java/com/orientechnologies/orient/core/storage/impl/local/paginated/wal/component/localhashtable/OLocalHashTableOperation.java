package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.component.localhashtable;

import com.orientechnologies.common.serialization.types.OStringSerializer;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.OOperationUnitId;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.component.OComponentOperation;

import java.nio.ByteBuffer;
import java.util.Objects;

public class OLocalHashTableOperation extends OComponentOperation {
  private String name;

  @SuppressWarnings("WeakerAccess")
  public OLocalHashTableOperation() {
  }

  OLocalHashTableOperation(OOperationUnitId operationUnitId, String name) {
    super(operationUnitId);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public int toStream(byte[] content, int offset) {
    offset = super.toStream(content, offset);

    OStringSerializer.INSTANCE.serializeNativeObject(name, content, offset);
    offset += OStringSerializer.INSTANCE.getObjectSize(name);

    return offset;
  }

  @Override
  public int fromStream(byte[] content, int offset) {
    offset = super.fromStream(content, offset);
    name = OStringSerializer.INSTANCE.deserializeNativeObject(content, offset);
    offset += OStringSerializer.INSTANCE.getObjectSize(name);

    return offset;
  }

  @Override
  public void toStream(ByteBuffer buffer) {
    super.toStream(buffer);

    OStringSerializer.INSTANCE.serializeInByteBufferObject(name, buffer);
  }

  @Override
  public int serializedSize() {
    return super.serializedSize() + OStringSerializer.INSTANCE.getObjectSize(name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    OLocalHashTableOperation that = (OLocalHashTableOperation) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name);
  }
}
