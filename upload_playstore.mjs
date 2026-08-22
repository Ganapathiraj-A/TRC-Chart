import { google } from '/home/ganapathiraj/Code/Android/SriBagavath/node_modules/googleapis/build/src/index.js';
import fs from 'fs';
import path from 'path';

async function uploadToPlayStore() {
    const packageName = "com.example.trcchart";
    const authKeyPath = path.resolve(process.cwd(), 'service-account.json');
    const aabPath = path.resolve(process.cwd(), 'app/build/outputs/bundle/release/app-release.aab');
    const trackName = process.argv[2] || process.env.PLAYSTORE_TRACK || 'internal';

    console.log(`Starting Play Store upload for ${packageName}...`);
    console.log(`Track: ${trackName}`);
    console.log(`Artifact: ${aabPath}`);

    if (!fs.existsSync(authKeyPath)) {
        console.error("ERROR: service-account.json not found in project root.");
        process.exit(1);
    }

    if (!fs.existsSync(aabPath)) {
        console.error(`ERROR: AAB file not found at ${aabPath}`);
        process.exit(1);
    }

    try {
        const auth = new google.auth.GoogleAuth({
            keyFile: authKeyPath,
            scopes: ['https://www.googleapis.com/auth/androidpublisher']
        });

        const publisher = google.androidpublisher({
            version: 'v3',
            auth
        });

        console.log("Creating new edit in Play Console...");
        const edit = await publisher.edits.insert({
            packageName
        });
        const editId = edit.data.id;

        console.log("Uploading AAB bundle...");
        const bundle = await publisher.edits.bundles.upload({
            editId,
            packageName,
            media: {
                mimeType: 'application/octet-stream',
                body: fs.createReadStream(aabPath)
            }
        });

        const versionCode = bundle.data.versionCode;
        console.log(`✅ AAB uploaded successfully. Version Code: ${versionCode}`);

        console.log(`Assigning version ${versionCode} to track: ${trackName}...`);
        await publisher.edits.tracks.update({
            editId,
            packageName,
            track: trackName,
            requestBody: {
                releases: [
                    {
                        versionCodes: [versionCode.toString()],
                        status: 'completed'
                    }
                ]
            }
        });

        console.log("Committing changes...");
        await publisher.edits.commit({
            editId,
            packageName
        });

        console.log(`======================================`);
        console.log(`✅ SUCCESSFULLY PUBLISHED TO PLAY STORE!`);
        console.log(`Track: ${trackName}`);
        console.log(`======================================`);

    } catch (error) {
        console.error("❌ ERROR uploading to Play Store:");
        console.error(error.message);
        if (error.response && error.response.data) {
            console.error(JSON.stringify(error.response.data, null, 2));
        }
        process.exit(1);
    }
}

uploadToPlayStore();
