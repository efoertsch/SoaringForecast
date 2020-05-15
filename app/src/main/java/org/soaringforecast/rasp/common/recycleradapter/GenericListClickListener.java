package org.soaringforecast.rasp.common.recycleradapter;

// TODO - don't think I need position in listener - check and remove
public interface GenericListClickListener<T> {

    void onItemClick(T object, int position);
}
