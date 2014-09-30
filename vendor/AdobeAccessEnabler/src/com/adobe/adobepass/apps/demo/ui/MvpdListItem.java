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

package com.adobe.adobepass.apps.demo.ui;

import com.adobe.adobepass.accessenabler.models.Mvpd;

public class MvpdListItem {
	private final Mvpd mvpd;

    public MvpdListItem(Mvpd mvpd) { this.mvpd = mvpd; }

	public Mvpd getMvpd() { return mvpd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MvpdListItem that = (MvpdListItem) o;

        if (mvpd != null ? !mvpd.equals(that.mvpd) : that.mvpd != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mvpd != null ? mvpd.hashCode() : 0;
    }
}
