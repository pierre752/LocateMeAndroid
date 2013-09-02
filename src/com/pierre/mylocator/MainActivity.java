package com.pierre.mylocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity  {
	
	private GoogleMap map;
	private View mapContainer;
	private View saveAddressFormContainer;
	private View saveAddressButtonsContainer;
	private ProgressBar progressBar;
	private Dialog listDialog;
	private Dialog layerDialog;
	private Dialog resetDialog;
	private Button addAddressButton;
	private Button saveButton;
	private Button cancelButton;
	private EditText saveAddressFormName;
	private EditText saveAddressFormAddress1;
	private EditText saveAddressFormAddress2;
	private EditText saveAddressFormZip;
	private EditText saveAddressFormCity;
	private EditText saveAddressFormState;
	private EditText saveAddressFormCountry;
	private SQLHelper sqlHelper;
	private double latitude;
	private double longitude;
	private ArrayList<AddressData> addressDataList;
	private ArrayAdapter<AddressData> listArrayAdapter;
	private HashMap<String, Marker> markerMap;
	private HashMap<String, AddressData> addressDataMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sqlHelper = new SQLHelper(this);
		map = ((SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			View view = getLayoutInflater().inflate(R.layout.info_contents, null);
			
			@Override
			public View getInfoContents(Marker marker) {
				AddressData addressData = addressDataMap.get(marker.getId());
	            TextView nameTextView = ((TextView) view.findViewById(R.id.name));
	            if (!addressData.name.equals("")) {
	                nameTextView.setText(addressData.name);
	                nameTextView.setVisibility(View.VISIBLE);
	            } 
	            else {
	            	nameTextView.setVisibility(View.GONE);
	            }
	            TextView address1TextView = ((TextView) view.findViewById(R.id.address1));
	            if (!addressData.address1.equals("")) {
	            	address1TextView.setText(addressData.address1);
	            	address1TextView.setVisibility(View.VISIBLE);
	            } 
	            else {
	            	address1TextView.setVisibility(View.GONE);
	            }
	            TextView address2TextView = ((TextView) view.findViewById(R.id.address2));
	            if (!addressData.address2.equals("")) {
	            	address2TextView.setText(addressData.address2);
	            	address2TextView.setVisibility(View.VISIBLE);
	            } 
	            else {
	            	address2TextView.setVisibility(View.GONE);
	            }
	            TextView cityAndZipTextView = ((TextView) view.findViewById(R.id.city_and_zip));
	            if (!addressData.city.equals("")) {
	            	cityAndZipTextView.setText(addressData.city + " " + addressData.zip);
	            	cityAndZipTextView.setVisibility(View.VISIBLE);
	            } 
	            else {
	            	if (!addressData.zip.equals("")) {
		            	cityAndZipTextView.setText(addressData.zip);
	            	}
	            	else {
	            		cityAndZipTextView.setVisibility(View.GONE);
	            	}
	            }
	            TextView countryTextView = ((TextView) view.findViewById(R.id.country));
	            if (!addressData.country.equals("")) {
	            	countryTextView.setText(addressData.country);
	            	countryTextView.setVisibility(View.VISIBLE);
	            } 
	            else {
	            	countryTextView.setVisibility(View.GONE);
	            }
	            TextView locationTextView = ((TextView) view.findViewById(R.id.location));
	            locationTextView.setText(addressData.location.latitude + "; " + addressData.location.longitude);
	            return view;
			}

			@Override
			public View getInfoWindow(Marker marker) {
				// TODO Auto-generated method stub
				return null;
			}
			
		});
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				clickOnMarker(addressDataMap.get(marker.getId()));
				return true;
			}
			
		});
		map.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {

			@Override
			public void onMyLocationChange(Location location) {    
	            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
	            map.setOnMyLocationChangeListener(null);
			}
			
		});
		
		mapContainer = findViewById(R.id.map_container);
		saveAddressFormContainer = findViewById(R.id.save_address_form_container);
		saveAddressButtonsContainer = findViewById(R.id.save_address_buttons_container);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		saveAddressFormName = (EditText) findViewById(R.id.editText_name);
		saveAddressFormAddress1 = (EditText) findViewById(R.id.editText_address1);
		saveAddressFormAddress2 = (EditText) findViewById(R.id.editText_address2);
		saveAddressFormZip = (EditText) findViewById(R.id.editText_zip);
		saveAddressFormZip.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				saveAddressFormAddress1.setText("");
				saveAddressFormAddress2.setText("");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				
			}
	        
	    }); 
		saveAddressFormCity = (EditText) findViewById(R.id.editText_city);
		saveAddressFormCity.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				saveAddressFormAddress1.setText("");
				saveAddressFormAddress2.setText("");
				saveAddressFormZip.setText("");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				
			}
	        
	    });
		saveAddressFormState = (EditText) findViewById(R.id.editText_state);
		saveAddressFormState.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				saveAddressFormAddress1.setText("");
				saveAddressFormAddress2.setText("");
				saveAddressFormZip.setText("");
				saveAddressFormCity.setText("");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				
			}
	        
	    });
		saveAddressFormCountry = (EditText) findViewById(R.id.editText_country);
		saveAddressFormCountry.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				saveAddressFormAddress1.setText("");
				saveAddressFormAddress2.setText("");
				saveAddressFormZip.setText("");
				saveAddressFormCity.setText("");
				saveAddressFormState.setText("");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				
			}
	        
	    });
		
		AsyncTask<Void, Void, ArrayList<AddressData>> asyncTask = new AsyncTask<Void, Void, ArrayList<AddressData>>() {

			@Override
			protected ArrayList<AddressData> doInBackground(Void... arg0) {
				SQLiteDatabase db = sqlHelper.getReadableDatabase();
				String[] projection = {
					SQLHelper.AddressEntry._ID,
					SQLHelper.AddressEntry.COLUMN_NAME_LATITUDE,
					SQLHelper.AddressEntry.COLUMN_NAME_LONGITUDE,
					SQLHelper.AddressEntry.COLUMN_NAME_NAME,
					SQLHelper.AddressEntry.COLUMN_NAME_ADDRESS1,
					SQLHelper.AddressEntry.COLUMN_NAME_ADDRESS2,
					SQLHelper.AddressEntry.COLUMN_NAME_ZIP,
					SQLHelper.AddressEntry.COLUMN_NAME_CITY,
					SQLHelper.AddressEntry.COLUMN_NAME_STATE,
					SQLHelper.AddressEntry.COLUMN_NAME_COUNTRY
				};
				Cursor cursor = db.query(SQLHelper.AddressEntry.TABLE_NAME, projection, null, null, null, null, null);
				ArrayList<AddressData> data = new ArrayList<AddressData>();
				while (cursor.moveToNext()) {
					double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_LATITUDE));
					double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_LONGITUDE));
					String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_NAME));
					String address1 = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_ADDRESS1));
					String address2 = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_ADDRESS2));
					String zip = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_ZIP));
					String city = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_CITY));
					String state = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_STATE));
					String country = cursor.getString(cursor.getColumnIndexOrThrow(SQLHelper.AddressEntry.COLUMN_NAME_COUNTRY));
					AddressData addressData = new AddressData(new LatLng(latitude, longitude), name, address1, address2, zip, city, state, country);
					data.add(addressData);
				}
				db.close();
				return data;
			}
			
			@Override
		    protected void onPostExecute(ArrayList<AddressData> result) {
				for (AddressData addressData : result) {
					addMarker(addressData);
				}
			}
			
		};
		asyncTask.execute();
		
		ArrayAdapter<String> layerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, new String [] {"None", "Normal", "Satellite", "Terrain", "Hybrid"});
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Chose layer:");
		builder.setInverseBackgroundForced(true);
		final ListView layerListView = new ListView(this);
		layerListView.setBackgroundColor(Color.WHITE);
		layerListView.setCacheColorHint(0);
		layerListView.setAdapter(layerArrayAdapter);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		layerListView.setOnItemClickListener(new OnItemClickListener() {

			int state = 1;
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (state != position) {
					map.setMapType(position);
					((CheckedTextView) parent.getChildAt(state)).setChecked(false);
					((CheckedTextView) view).setChecked(true);
					state = position;
				}
				layerDialog.dismiss();
			}
			
		});
		builder.setView(layerListView);
		layerDialog = builder.create();
		layerDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialogInterface) {
				((CheckedTextView) layerListView.getChildAt(1)).setChecked(true);
				layerDialog.setOnShowListener(null);
			}
			
		});
		
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure?");
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {

					@Override
					protected Integer doInBackground(Void... arg0) {
						SQLiteDatabase db = sqlHelper.getReadableDatabase();
						int deletedRows = db.delete(SQLHelper.AddressEntry.TABLE_NAME, null, null);
						db.close();
						return deletedRows;
					}
					
					@Override
				    protected void onPostExecute(Integer result) {
						Toast.makeText(MainActivity.this, result + (result == 1 ? " address deleted" : " addresses deleted"), Toast.LENGTH_SHORT).show();
						if (result > 0) {
							for (Marker marker : markerMap.values()) {
								marker.remove();
							}
							listArrayAdapter.clear();
							listArrayAdapter.add(new AddressData(new LatLng(0, 0), "Add address", "", "", "", "", "", ""));
							markerMap.clear();
							addressDataMap.clear();
							addAddressButton.setText(getResources().getString(R.string.button_add_address));
							addAddressButton.setTextColor(Color.RED);
						}
					}
					
				};
				asyncTask.execute();
		        dialog.dismiss();
		    }
		    
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		    
		});
		resetDialog = builder.create();
		
		markerMap = new HashMap<String, Marker>();
		addressDataMap = new HashMap<String, AddressData>();
		addressDataList = new ArrayList<AddressData>();
		addressDataList.add(new AddressData(new LatLng(0, 0), "Add address", "", "", "", "", "", ""));
		listArrayAdapter = new ArrayAdapter<AddressData>(this, android.R.layout.simple_list_item_1, android.R.id.text1, addressDataList) {
			
			@Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = super.getView(position, convertView, parent);
	            if (position == 0) {
	            	((TextView) view).setTextColor(Color.RED);
	            	((TextView) view).setTypeface(null, Typeface.BOLD);
	            }
	            else {
	            	((TextView) view).setTextColor(Color.GREEN);
	            }
	            return view;
	        }
			
		};
		builder = new AlertDialog.Builder(this);
		builder.setInverseBackgroundForced(true);
		ListView listListView = new ListView(this);
		listListView.setBackgroundColor(Color.WHITE);
		listListView.setCacheColorHint(0);
		listListView.setAdapter(listArrayAdapter);
		listListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					AsyncTask<LatLng, Integer, List<String>> asyncTask = new AsyncTask<LatLng, Integer, List<String>>() {

						@Override
						protected void onPreExecute () {
							progressBar.setVisibility(View.VISIBLE);
							addAddressButton.setVisibility(View.GONE);
							map.getUiSettings().setAllGesturesEnabled(false);
							map.getUiSettings().setMyLocationButtonEnabled(false);
							map.getUiSettings().setZoomControlsEnabled(false);
						}
						
						@Override
						protected List<String> doInBackground(LatLng... params) {
							Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

					        LatLng latLng = params[0];
					        List<Address> addresses = null;
					        List<String> addressValue = new ArrayList<String>();
					        try {
					            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
					        } 
					        catch (IOException e) {
					            e.printStackTrace();
					        }
					        if (addresses != null && addresses.size() > 0) {
					            Address address = addresses.get(0);
					            
					            // Capture latitude & longitude of address
					            latitude = latLng.latitude;
					            longitude = latLng.longitude;
					            
					            // Address 1
					            String feature = "";
					            if (address.getFeatureName() != null) {
					            	feature += address.getFeatureName() + " ";
					            }
					            if (address.getThoroughfare() != null) {
					            	feature += address.getThoroughfare() + " ";
					            }
					            if (address.getSubLocality() != null) {
					            	feature += address.getSubLocality();
					            }
					            addressValue.add(feature);
					            
					            // Address 2
					            feature = "";
					            if (address.getPremises() != null) {
					            	feature += address.getPremises();
					            }
					            addressValue.add(feature);
					            
					            // Zip
					            feature = "";
					            if (address.getPostalCode() != null) {
					            	feature += address.getPostalCode();
					            }
					            addressValue.add(feature);
					            
					            // City
					            feature = "";
					            if (address.getLocality() != null) {
					            	feature += address.getLocality();
					            }
					            addressValue.add(feature);
					            
					            // State
					            feature = "";
					            if (address.getAdminArea() != null) {
					            	feature += address.getAdminArea();
					            }
					            addressValue.add(feature);
					            
					            // Country
					            feature = "";
					            if (address.getCountryName() != null) {
					            	feature += address.getCountryName();
					            }
					            addressValue.add(feature);
					        }
					        return addressValue;
						}
						
						@Override
						protected void onProgressUpdate(Integer... progress) {
					        
					    }

						@Override
					    protected void onPostExecute(List<String> result) {
							if (result.size() > 5) {
								saveAddressFormContainer.setVisibility(View.VISIBLE);
								saveAddressButtonsContainer.setVisibility(View.VISIBLE);
								saveAddressFormName.requestFocus();
								saveAddressFormCountry.setText(result.get(5));
								saveAddressFormState.setText(result.get(4));
								saveAddressFormCity.setText(result.get(3));
								saveAddressFormZip.setText(result.get(2));
								saveAddressFormAddress2.setText(result.get(1));
								saveAddressFormAddress1.setText(result.get(0));
							}
							else {
								Toast.makeText(MainActivity.this, "Error, please try again!", Toast.LENGTH_SHORT).show();
								map.getUiSettings().setAllGesturesEnabled(true);
								map.getUiSettings().setMyLocationButtonEnabled(true);
								map.getUiSettings().setZoomControlsEnabled(true);
								addAddressButton.setVisibility(View.VISIBLE);
							}
							progressBar.setVisibility(View.GONE);
					    }
						
					};
					asyncTask.execute(map.getCameraPosition().target);
					addAddressButton.setText(getResources().getString(R.string.button_add_address));
					addAddressButton.setTextColor(Color.RED);
				}
				else {
					AddressData addressData = addressDataList.get(position);
					clickOnMarker(addressData);
					addAddressButton.setText(addressData.name + " ^");
					addAddressButton.setTextColor(Color.GREEN);
				}
				listDialog.dismiss();
			}
			
		});
		builder.setView(listListView);
		listDialog = builder.create();
		
		addAddressButton = (Button) findViewById(R.id.button_add_address);
		addAddressButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				listDialog.show();
			}
		});
		
		saveButton = (Button) findViewById(R.id.button_save);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (saveAddressFormName.getText().toString().equals("")) {
					Toast.makeText(MainActivity.this, "Please enter name!", Toast.LENGTH_SHORT).show();
				}
				else {
					AsyncTask<Void, Void, AddressData> asyncTask = new AsyncTask<Void, Void, AddressData>() {
	
						@Override
						protected AddressData doInBackground(Void... arg0) {
							SQLiteDatabase db = sqlHelper.getWritableDatabase();
							ContentValues values = new ContentValues();
							String name = saveAddressFormName.getText().toString();
							String address1 = saveAddressFormAddress1.getText().toString();
							String address2 = saveAddressFormAddress2.getText().toString();
							String zip = saveAddressFormZip.getText().toString();
							String city = saveAddressFormCity.getText().toString();
							String state = saveAddressFormState.getText().toString();
							String country = saveAddressFormCountry.getText().toString();
							AddressData addressData = new AddressData(new LatLng(latitude, longitude), name, address1, address2, zip, city, state, country);
							
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_LATITUDE, latitude);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_LONGITUDE, longitude);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_NAME, name);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_ADDRESS1, address1);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_ADDRESS2, address2);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_ZIP, zip);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_CITY, city);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_STATE, state);
							values.put(SQLHelper.AddressEntry.COLUMN_NAME_COUNTRY, country);
							long newRowId = db.insert(SQLHelper.AddressEntry.TABLE_NAME, null, values);
							db.close();
							if (newRowId == -1) {
								addressData = null;
							}
							return addressData;
						}
						
						@Override
					    protected void onPostExecute(AddressData result) {
							if (result != null) {
								Toast.makeText(MainActivity.this, "Address saved successfully!", Toast.LENGTH_LONG).show();
								addMarker(result);
								saveAddressFormName.setText("");
								addAddressButton.setText(result.name + " ^");
								addAddressButton.setTextColor(Color.GREEN);
							}
							else {
								Toast.makeText(MainActivity.this, "Unable to save address!", Toast.LENGTH_LONG).show();
							}
						}
						
					};
					asyncTask.execute();
					returnToMap();
				}
			}
			
		});
		
		cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				returnToMap();
				saveAddressFormName.setText("");
			}
			
		});
		
		final View rootView = findViewById(R.id.root);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @SuppressWarnings("deprecation")
			@Override
		    public void onGlobalLayout() {
		        if (rootView.getRootView().getHeight() - rootView.getHeight() > getWindowManager().getDefaultDisplay().getHeight()/3) { 
		        	mapContainer.setVisibility(View.GONE);
		        }
		        else {
		        	mapContainer.setVisibility(View.VISIBLE);
		        }
		     }
		});
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, Menu.NONE, "Set layer type");
		menu.add(Menu.NONE, 2, Menu.NONE, "Reset locations");
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
			case 1:
				layerDialog.show();
				break;
			case 2:
				resetDialog.show();
				break;
			default:
			    break;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	protected void returnToMap() {
		saveAddressFormContainer.setVisibility(View.GONE);
		saveAddressButtonsContainer.setVisibility(View.GONE);
		addAddressButton.setVisibility(View.VISIBLE);
		map.getUiSettings().setAllGesturesEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(saveAddressFormName.getWindowToken(), 0);
	}
	
	protected void addMarker(AddressData addressData) {
		listArrayAdapter.insert(addressData, 1);
		Marker marker = map.addMarker((new MarkerOptions().position(addressData.location).title(addressData.name)));
		markerMap.put(addressData.location.latitude + " " + addressData.location.longitude, marker);
		addressDataMap.put(marker.getId(), addressData);
	}
	
	protected void clickOnMarker(final AddressData addressData) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(addressData.location, 15), new GoogleMap.CancelableCallback() {

			@Override
			public void onCancel() {
				
			}

			@Override
			public void onFinish() {
				markerMap.get(addressData.location.latitude + " " + addressData.location.longitude).showInfoWindow();
			}
			
		});
	}
	
	
	
	
	protected String getAddress(double latitude, double longitude) {
		String address = null;
		try {
		    Geocoder geocoder = new Geocoder(getApplicationContext());
		    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
		    if (!addresses.isEmpty()) {
		    	address = "Name: ";
		    		address += addresses.get(0).getFeatureName() + "\n";
		    		address += "Premises: " + addresses.get(0).getPremises() + "\n";
		    		address += "SubLocality: " + addresses.get(0).getSubLocality() + "\n";
		    		address += "Locality: " + addresses.get(0).getLocality() + "\n";
		    		address += "SubThoroughfare: " + addresses.get(0).getSubThoroughfare() + "\n";
		    		address += "Thoroughfare: " + addresses.get(0).getThoroughfare() + "\n";
		    		address += "SubAdminArea: " + addresses.get(0).getSubAdminArea() + "\n";
		    		address += "AdminArea: " + addresses.get(0).getAdminArea() + "\n";
		    		address += "Phone: " + addresses.get(0).getPhone() + "\n";
		    		address += "CountryCode: " + addresses.get(0).getCountryCode() + "\n";
		    		address += "CountryName: " + addresses.get(0).getCountryName() + "\n";
		    		address += "Locale: " + addresses.get(0).getLocale() + "\n";
		    		address += "PostalCode: " + addresses.get(0).getPostalCode() + "\n";
		    }
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		return address;
	}

}
