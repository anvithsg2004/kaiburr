CI–CD: Build Java app with Maven and publish Docker image to GHCR

This pipeline runs on every push to main (and on PRs), builds the Spring Boot JAR with Maven Wrapper, then builds and pushes a Docker image to GitHub Container Registry (GHCR).


What I used

GitHub repository (public)

GitHub Actions (hosted runner)

GHCR (GitHub’s free container registry)

No external secrets required (uses the built-in GITHUB_TOKEN)

1) One-time setup

Push code to GitHub (already done)

git init
git remote add origin git@github.com:anvithsg2004/kaiburr.git
git add .
git commit -m "Initial commit"
git branch -m master main
git push -u origin main


Create workflow file

Create .github/workflows/ci.yml with:

name: CI (Maven + Docker → GHCR)

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch: {}

permissions:
  contents: read
  packages: write   # allow pushing to GHCR

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build-and-push:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Temurin JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven

      # Build JAR (skip tests to avoid Mongo dependency on CI)
      - name: Build with Maven
        run: ./mvnw -B -ntp clean package -DskipTests

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract Docker metadata (tags, labels)
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ghcr.io/${{ github.repository }}
          tags: |
            type=raw,value=latest,enable={{is_default_branch}}
            type=sha

      - name: Set up Buildx
        uses: docker/setup-buildx-action@v3

      - name: Build and push image
        uses: docker/build-push-action@v6
        with:
          context: .
          file: ./Dockerfile
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}


Make the Maven wrapper executable + normalize line endings (you already did this fix after the first failed run):

# ensure wrapper is executable and uses LF (so ubuntu runner can run it)
Add-Content .gitattributes "`n*.sh text eol=lf`n mvnw text eol=lf" -NoNewline
git add .gitattributes
git add --renormalize .
git update-index --chmod=+x mvnw
git commit -m "Normalize LF endings for mvnw and mark executable"
git push


Actions settings (in the repo → Settings → Actions):

Actions permissions: Allow all actions and reusable workflows ✅

Workflow permissions: Read and write permissions ✅ (click Save)

2) What happens on a run

actions/checkout pulls the code

actions/setup-java installs JDK 17 and configures Maven cache

./mvnw clean package -DskipTests builds the Spring Boot fat JAR

Docker Buildx builds the image using your Dockerfile

Login to ghcr.io happens using GITHUB_TOKEN

Image is pushed with tags

✅ You saw a green run and a Docker Build summary artifact.

3) How I verified

In Actions: the job status Success, with a “Docker Build summary”.

In Packages (Repo → Packages or Profile → Packages): image kaiburr visible.

Pull locally (optional):

echo $GH_PAT | docker login ghcr.io -u anvithsg2004 --password-stdin  # GH_PAT only needed if package is private
docker pull ghcr.io/anvithsg2004/kaiburr:latest
docker run -p 8080:8080 ghcr.io/anvithsg2004/kaiburr:latest

4) Common issues I hit (and fixes)

Exit code 126 / “permission denied” for ./mvnw
→ Fix file mode + LF endings (see “one-time setup” step 3).

Denied pushing to GHCR
→ Ensure Workflow permissions = Read and write in repo settings, and permissions.packages=write in workflow.

Image not visible publicly
→ Make the GHCR package Public (Package settings) or use an imagePullSecret in Kubernetes.

5) (Optional) Deploy the CI image to Kubernetes

Update your deployment to use the CI image:

image: ghcr.io/anvithsg2004/kaiburr:latest
imagePullPolicy: Always


Apply and rollout:

kubectl -n taskrun apply -f k8s/30-app.yaml
kubectl -n taskrun rollout status deploy/taskrun

6) Triggering the pipeline again

Push a new commit to main, or

Go to Actions → CI (Maven + Docker → GHCR) → Run workflow (manual dispatch)

7) Summary

✅ GitHub Actions builds JAR via Maven Wrapper

✅ Docker image is built and pushed to GHCR automatically


✅ Tag latest (and per-commit SHA) always available for Kubernetes or local testing

