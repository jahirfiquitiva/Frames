BRANCH="master"

if [ "$TRAVIS_BRANCH" = "$BRANCH" ]; then
  if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  	if [ -z "$TRAVIS_TAG" ]; then
	    echo -e "Starting to tag commit.\n"

	    git config --global user.email "travis@travis-ci.org"
	    git config --global user.name "Travis"

	    # Add tag and push to master.
	    git tag -a v${TRAVIS_BUILD_NUMBER} -m "Travis build $TRAVIS_BUILD_NUMBER pushed a tag."
	    git push origin --tags
	    git fetch origin

	    echo -e "Done magic with tags.\n"
	fi
  fi
fi
