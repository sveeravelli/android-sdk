/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui.storageviewer;

import java.util.ArrayList;
import java.util.List;

import com.adobe.adobepass.apps.demo.R;
import com.adobe.adobepass.apps.demo.ui.AbstractActivity;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StorageViewerActivity extends AbstractActivity {
    private TreeViewList treeView;

    private TreeStateManager<Long> manager = null;
    private boolean collapsible;

    private StorageHelper storageHelper = new StorageHelper();
    private int selectedStorage;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedStorage = StorageHelper.CURRENT_STORAGE_VERSION;

        changeActivityTitle();
        setContentView(R.layout.storage_viewer);

        refreshTreeView(selectedStorage);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable("treeManager", manager);
        outState.putBoolean("collapsible", this.collapsible);
        super.onSaveInstanceState(outState);
    }

    protected final void setCollapsible(final boolean newCollapsible) {
        this.collapsible = newCollapsible;
        treeView.setCollapsible(this.collapsible);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.storage_viewer_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.expand_all_menu_item) {
            manager.expandEverythingBelow(null);
        } else if (item.getItemId() == R.id.collapse_all_menu_item) {
            manager.collapseChildren(null);
        } else if (item.getItemId() == R.id.clear_all_menu_item) {
            storageHelper.clearStorageAll();
            refreshTreeView(selectedStorage);
        } else if (item.getItemId() == R.id.clear_storage1_menu_item) {
            storageHelper.clearStorage(1);
            if (selectedStorage == 1)
                refreshTreeView(selectedStorage);
        } else if (item.getItemId() == R.id.clear_storage2_menu_item) {
            storageHelper.clearStorage(2);
            if (selectedStorage == 2)
                refreshTreeView(selectedStorage);
        } else if (item.getItemId() == R.id.clear_storage3_menu_item) {
            storageHelper.clearStorage(3);
            if (selectedStorage == 3)
                refreshTreeView(selectedStorage);
        } else if (item.getItemId() == R.id.show_storage1_menu_item) {
            selectedStorage = 1;
            changeActivityTitle();
            refreshTreeView(selectedStorage);
        } else if (item.getItemId() == R.id.show_storage2_menu_item) {
            selectedStorage = 2;
            changeActivityTitle();
            refreshTreeView(selectedStorage);
        } else if (item.getItemId() == R.id.show_storage3_menu_item) {
            selectedStorage = 3;
            changeActivityTitle();
            refreshTreeView(selectedStorage);
        }

        return true;
    }

    private void refreshTreeView(int version) {
        List<StorageHelper.TreeNode> nodes = storageHelper.readStorage(version);

        manager = new InMemoryTreeStateManager<Long>();
        final TreeBuilder<Long> treeBuilder = new TreeBuilder<Long>(manager);
        for (StorageHelper.TreeNode node : nodes) {
            treeBuilder.sequentiallyAddNextNode(node.getId(), node.getLevel());
        }

        treeView = (TreeViewList) findViewById(R.id.mainTreeView);
        List<String> labels = new ArrayList<String>();
        int maxLevel = 0;
        for (StorageHelper.TreeNode node : nodes) {
            labels.add(node.getLabel());
            if (node.getLevel() > maxLevel)
                maxLevel = node.getLevel();
        }
        treeView.setAdapter(new SimpleStandardAdapter(this, manager, maxLevel + 1, labels));

        setCollapsible(true);
    }

    void changeActivityTitle() {
        setTitle("Storage v" + selectedStorage);
    }
}
