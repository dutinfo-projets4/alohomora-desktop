

import fr.alohomora.App;
import fr.alohomora.Configuration;
import fr.alohomora.database.Database;
import fr.alohomora.model.Challenge;
import fr.alohomora.model.Config;
import fr.alohomora.model.Element;
import fr.alohomora.model.User;
import fr.alohomora.model.apiservice.Api;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Alohomora Password Manager
 * Copyright (C) 2018 Team Alohomora
 * Léo BERGEROT, Sylvain COMBRAQUE, Sarah LAMOTTE, Nathan JANCZEWSKI
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **/
public class UserTest {

	private Api API;

	@Before
	public void init(){
		Configuration.load(new String[]{});
		this.API = new Api();
	}

	@Test
	public void getChallengeCheckCode() {
		Call<Challenge> call = this.API.getChallenge();
		try {
			Response<Challenge> response = call.execute();
			assertEquals(response.code(), 200);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void getChallengeCheckChallenge() {
		Call<Challenge> call = this.API.getChallenge();
		try {
			Response<Challenge> response = call.execute();
			assertNotNull(response.body().getChallenge());
			assertNotNull(response.body().getID());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void checkUserConnexion() {
		String challenge, challengeID = "0";
		String passCodeHash = null;
		try {
			Call<Challenge> call = this.API.getChallenge();
			Response<Challenge> response = call.execute();
			challenge = response.body().getChallenge();
			challengeID = response.body().getID();
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			// hash(username)
			String usernameHash = DatatypeConverter.printHexBinary(messageDigest.digest("toto".getBytes())).toLowerCase();
			// hash(password)
			String passwordHash = DatatypeConverter.printHexBinary(messageDigest.digest("toto".getBytes())).toLowerCase();
			// hash(passwordHash)
			System.out.println(passwordHash);
			passwordHash = DatatypeConverter.printHexBinary(messageDigest.digest(passwordHash.getBytes())).toLowerCase();
			System.out.println(passwordHash);
			String passcode = usernameHash.toLowerCase() + challenge + passwordHash.toLowerCase();

			//hash(passcode)
			passCodeHash = DatatypeConverter.printHexBinary(messageDigest.digest(passcode.getBytes())).toLowerCase();
			System.out.println(passCodeHash);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (passCodeHash != null){
			Pair<String, String> a[] = new Pair[]{
					new Pair("passcode", passCodeHash.toLowerCase()),
					new Pair("challenge", challengeID),
					new Pair("public_key", "1"),
					new Pair("machine_name", "1")
			};

			Call<User> call = this.API.connect(a);
			try {
				Response<User> response = call.execute();
				System.out.println(response.code());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void addElement(){
		Pair<String, String> [] param = new Pair[]{
				new Pair("parent_grp", "123"),
				new Pair("content", "test")
		};
		Configuration.LOGIN_TOKEN = Database.getInstance().getToken();

		Call<Element> call = this.API.addElement(param);
		Response <Element> response = null;
		try {
			 response = call.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.print(response.code());
	}



}

