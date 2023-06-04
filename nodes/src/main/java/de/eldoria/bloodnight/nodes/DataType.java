package de.eldoria.bloodnight.nodes;

/**
 * Class defining different data types.
 */
public enum DataType {
    EVENT,
    ENTITY,
    CANCELABLE_EVENT,
    INTEGER,
    NUMBER(INTEGER),
    STRING,
    UUID,
    WORLD,
    BOOLEAN,
    VECTOR,
    LOCATION(VECTOR),
    DAMAGE_CAUSE,
    ENTITY_TYPE,
    POTION_TYPE,
    ENCHANTMENT_TYPE,
    BLOCK_TYPE,
    ITEM_TYPE,
    ANY,
    LINKED;

    private final DataType[] subtypes;

    DataType() {
        this(new DataType[0]);
    }

    DataType(DataType... subtypes) {
        this.subtypes = subtypes;
    }

    public DataType[] subtypes() {
        if (this == ANY) {
            return values();
        }
        return subtypes;
    }

    public boolean isType(DataType dataType) {
        if (dataType == this) return true;
        for (DataType subtype : dataType.subtypes()) {
            if (subtype == this) return true;
        }
        return false;
    }
}
