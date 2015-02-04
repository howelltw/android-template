/*
 * CrossDatabaseQueryBaseManager.java
 *
 * GENERATED FILE - DO NOT EDIT
 * 
 */



package org.company.project.domain.attached.crossdatabasequery;

import org.dbtools.android.domain.AndroidBaseManager;
import org.company.project.domain.DatabaseManager;
import org.dbtools.android.domain.database.DatabaseWrapper;


@SuppressWarnings("all")
public abstract class CrossDatabaseQueryBaseManager extends AndroidBaseManager<CrossDatabaseQuery> {

    @javax.inject.Inject
     DatabaseManager databaseManager;
    @javax.inject.Inject
     com.squareup.otto.Bus bus;

    public CrossDatabaseQueryBaseManager() {
    }

    @javax.annotation.Nonnull
    public String getDatabaseName() {
        return CrossDatabaseQuery.DATABASE;
    }

    @javax.annotation.Nonnull
    public CrossDatabaseQuery newRecord() {
        return new CrossDatabaseQuery();
    }

    @javax.annotation.Nonnull
    public String[] getAllKeys() {
        return CrossDatabaseQuery.ALL_KEYS;
    }

    @javax.annotation.Nonnull
    public DatabaseWrapper getReadableDatabase(@javax.annotation.Nonnull String databaseName) {
        return databaseManager.getReadableDatabase(databaseName);
    }

    @javax.annotation.Nonnull
    public DatabaseWrapper getReadableDatabase() {
        return databaseManager.getReadableDatabase(getDatabaseName());
    }

    @javax.annotation.Nonnull
    public DatabaseWrapper getWritableDatabase(@javax.annotation.Nonnull String databaseName) {
        return databaseManager.getWritableDatabase(databaseName);
    }

    @javax.annotation.Nonnull
    public DatabaseWrapper getWritableDatabase() {
        return databaseManager.getWritableDatabase(getDatabaseName());
    }

    @javax.annotation.Nonnull
    public org.dbtools.android.domain.AndroidDatabase getAndroidDatabase(@javax.annotation.Nonnull String databaseName) {
        return databaseManager.getDatabase(databaseName);
    }

    public abstract String getQuery();

    @javax.annotation.Nonnull
    public String getTableName() {
        return getQuery();
    }

    @javax.annotation.Nonnull
    public String getPrimaryKey() {
        return null;
    }

    @javax.annotation.Nonnull
    public String getDropSql() {
        return "";
    }

    @javax.annotation.Nonnull
    public String getCreateSql() {
        return "";
    }

    @Override
    public boolean save(@javax.annotation.Nonnull String databaseName, @javax.annotation.Nonnull CrossDatabaseQuery e) {
        throw new IllegalStateException("Cannot call SAVE on a CrossDatabaseQuery View or Query");
    }

    public com.squareup.otto.Bus getBus() {
        return bus;
    }

    public void setBus(com.squareup.otto.Bus bus) {
        this.bus = bus;
    }


}