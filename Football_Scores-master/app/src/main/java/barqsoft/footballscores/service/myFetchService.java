package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchService extends IntentService {
    public static final String LOG_TAG = "myFetchService";
    private Vector<ContentValues> mValues;
    private static int[] codes;

    public myFetchService() {
        super("myFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        codes = getResources().getIntArray(R.array.league_codes);

        mValues = new Vector<ContentValues>();

        getTeams();
        getFixtures("n2");
        getFixtures("p2");
    }

    private void getTeams() {

        for (int i = 0; i < codes.length; i++) {
            String teams_url = Uri.parse(getString(R.string.season_link)).buildUpon()
                    .appendPath(String.valueOf(codes[i]))
                    .appendPath(getString(R.string.teams)).build().toString();

            String teams_json = getData(teams_url);
            if (teams_json != null) {
                getCvalsTeams(teams_json);
            }
        }

    }


    private void getCvalsTeams(String team_json) {
        final String TEAMS = getString(R.string.teams);
        final String LINKS = "_links";
        final String SELF = "self";
        final String CREST_LOGO = "crestUrl";
        final String HREF_LINK = getString(R.string.base_url) + "/" + getString(R.string.teams) + "/";

        String team_id = null;
        String team_logo = null;


        try {
            JSONArray teams = new JSONObject(team_json).getJSONArray(TEAMS);

            if (teams.length() > 0) {
                for (int i = 0; i < teams.length(); i++) {
                    JSONObject team = teams.getJSONObject(i);
                    team_id = team.getJSONObject(LINKS).getJSONObject(SELF).getString("href");
                    team_id = team_id.replace(HREF_LINK, "");
                    int id_team = Integer.valueOf(team_id);

                    team_logo = team.getString(CREST_LOGO);

                    if (team_logo != null && team_logo.endsWith(".svg")) {
                        // Converting SVG to PNG More Info on :
                        // https://meta.wikimedia.org/wiki/SVG_image_support
                        team_logo = svgToPng(team_logo);
                    }
                    ContentValues teams_cvalues = new ContentValues();
                    teams_cvalues.put(DatabaseContract.scores_table.HOME_ID_COL, id_team);
                    teams_cvalues.put(DatabaseContract.scores_table.HOME_LOGO_COL, team_logo);
                    mValues.add(teams_cvalues);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String svgToPng(String svg_link) {
        String final_url;

        String pic = svg_link.substring(svg_link.lastIndexOf("/") + 1);
        int wiki_link_end = svg_link.indexOf("/wikipedia/") + 11;

        String end_path = svg_link.substring(wiki_link_end);
        int pos = wiki_link_end + end_path.indexOf("/") + 1;
        String local_path = svg_link.substring(pos);

        final_url = svg_link.substring(0, pos);
        final_url += "thumb/" + local_path;
        final_url += "/100px-" + pic + ".png";
        //Log.v("Got Final URL", final_url);
        return final_url;
    }

    private void getFixtures(String timeframe) {
        try {
            String fixtures_url = Uri.parse(getString(R.string.base_url)).buildUpon()
                    .appendPath(getString(R.string.fixtures))
                    .appendQueryParameter(getString(R.string.time_frame), timeframe).build().toString();

            String fixtures = getData(fixtures_url);

            if (fixtures != null) {
                getApplicationContext().getContentResolver().bulkInsert(DatabaseContract.BASE_CONTENT_URI, getCvalsFix(fixtures));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String getData(String url) {

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(url);
           // System.out.print(fetch);
            //URL fetch = new URL(BASE_URL);
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", Utilies.getPrefs(myFetchService.this));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            JSON_data = buffer.toString();
           // System.out.print(JSON_data);


        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }
        return JSON_data;
    }


    private ContentValues[] getCvalsFix(String fixtures) {

        ContentValues[] fixs = null;

        boolean isReal = true;

        final String SEASON_LINK = getString(R.string.season_link) + "/";
        final String MATCH_LINK = getString(R.string.match_link) + "/";
        final String TEAM_LINK = getString(R.string.team_link) + "/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";
        final String HOME_TEAM_ID = "homeTeam";
        final String AWAY_TEAM_ID = "awayTeam";

        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;
        String home_team_id = null;
        String away_team_id = null;

        try {
            JSONArray fixtures_arr = new JSONObject(fixtures).getJSONArray(FIXTURES);

            if (fixtures_arr.length() == 0) {
                isReal = false;
                fixtures = getString(R.string.dummy_data);
                fixtures_arr = new JSONObject(fixtures).getJSONArray(FIXTURES);
            }
            Vector<ContentValues> values = new Vector<ContentValues>(fixtures_arr.length());
            for (int i = 0; i < fixtures_arr.length(); i++) {
                JSONObject fix = fixtures_arr.getJSONObject(i);

                League = fix.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK, "");
                int league = Integer.valueOf(League);

                if (verify_data(codes, league)) {
                    match_id = fix.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");

                    home_team_id = fix.getJSONObject(LINKS).getJSONObject(HOME_TEAM_ID).getString("href");
                    home_team_id = home_team_id.replace(TEAM_LINK, "");
                    int home_id = Integer.parseInt(home_team_id);

                    away_team_id = fix.getJSONObject(LINKS).getJSONObject(AWAY_TEAM_ID).getString("href");
                    away_team_id = away_team_id.replace(TEAM_LINK, "");
                    int away_id = Integer.parseInt(away_team_id);

                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = fix.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));

                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    Home = fix.getString(HOME_TEAM);
                    Away = fix.getString(AWAY_TEAM);
                    Home_goals = fix.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = fix.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = fix.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.HOME_ID_COL, home_id);
                    match_values.put(DatabaseContract.scores_table.HOME_LOGO_COL, getTeamLogo(home_id));
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.AWAY_ID_COL, away_id);
                    match_values.put(DatabaseContract.scores_table.AWAY_LOGO_COL, getTeamLogo(away_id));
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                    values.add(match_values);
                }
                fixs = new ContentValues[values.size()];
                values.toArray(fixs);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fixs;
    }

    private String getTeamLogo(int team_id) {

        for (ContentValues cVal : mValues) {
            if (cVal.getAsInteger(DatabaseContract.scores_table.HOME_ID_COL).equals(team_id)) {
                // Log.v("Team Crest Logo URL", cVal.getAsString(DatabaseContract.scores_table.HOME_LOGO_COL));
                return cVal.getAsString(DatabaseContract.scores_table.HOME_LOGO_COL);

            }
        }

        return "";
    }

    private boolean verify_data(int[] arr, int val) {
        for (final int value : arr) {
            if (value == val) ;
            return true;
        }
        return false;
    }

}

