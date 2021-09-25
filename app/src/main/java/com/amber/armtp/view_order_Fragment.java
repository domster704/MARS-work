package com.amber.armtp;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class view_order_Fragment extends Fragment{
    Menu mainMenu;
    private android.support.v7.widget.Toolbar toolbar;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    public GlobalVars glbVars;
	public view_order_Fragment(){}
    View thisView;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_order_fragment, container, false);
        setHasOptionsMenu(true);
        thisView = rootView;
        glbVars.view = rootView;
        return rootView;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum);
        glbVars.nomenList = (GridView) getActivity().findViewById(R.id.listContrs);
        glbVars.PreviewZakaz();
        glbVars.fragManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_order_menu, menu);
        mainMenu = menu;
        if (glbVars.db.CheckForSales()>0){
            mainMenu.getItem(1).setEnabled(false);
            glbVars.setSaleIcon(mainMenu, 0, true);
        } else {
            mainMenu.getItem(1).setEnabled(true);
            glbVars.setSaleIcon(mainMenu, 0, false);
        }

        if (glbVars.isDiscount){
            glbVars.setSaleIcon(mainMenu, 1, true);
        } else {
            glbVars.setSaleIcon(mainMenu, 1, false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.GoToOrderHead:
                fragment = new order_head_Fragment();
                if (fragment != null) {
                    fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                    fragmentTransaction.commit();
                    toolbar.setTitle("Шапка заказа");
                }
                return true;
            case R.id.ClearOrderTB:
                glbVars.db.ClearOrderTb();
                glbVars.nomenList.setAdapter(null);
                setContrAndSum();
                return true;
            case R.id.OrderNomenSales:

                if (glbVars.isSales){
                    mainMenu.getItem(1).setEnabled(true);
                    glbVars.setSaleIcon(mainMenu, 0, false);
                } else {
                    glbVars.setDiscountIcon(mainMenu, 1, false);
                    glbVars.setSaleIcon(mainMenu, 0, true);
                }

                glbVars.db.calcSales(glbVars.db.GetContrID());
                if (glbVars.PreviewZakazAdapter!=null){
                    glbVars.myNom.requery();
                    glbVars.PreviewZakazAdapter.notifyDataSetChanged();
                }
                setContrAndSum();

                if (glbVars.isDiscount){
                    glbVars.isDiscount = false;
                    glbVars.Discount = 0;
                    mainMenu.getItem(2).setEnabled(false);
                    glbVars.setDiscountIcon(mainMenu, 2, false);
                }
                return true;
            case R.id.NomenDiscount:
                glbVars.CalculatePercentSale(mainMenu, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    private void setContrAndSum(){
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum);
    }
}
