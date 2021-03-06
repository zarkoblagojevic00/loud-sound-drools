<template>
    <div>
        <div class="top-nav">
            <div class="logo-nav">
                <img
                    class="logo-img"
                    alt="logo"
                    src="@/assets/loudsound-icon.png"
                />
                <div class="app-name gradient-text">LoudSound</div>
            </div>
        </div>
        <div class="container whole-page">
            <div class="content">
                <div class="main-column main-column-left">
                    <div class="choose-and-register-user">
                        <div class="choose-user content-node">
                            <div class="content-node-title-container">
                                Choose user
                            </div>
                            <div class="all-users-container">
                                <div
                                    v-for="(user, idx) in users"
                                    :key="idx"
                                    class="user underline-container transition-ease-in"
                                    :class="{
                                        'user-active':
                                            user.username === currentUser,
                                    }"
                                    @click="switchUser(user.username)"
                                >
                                    <div class="user-info-container">
                                        <div class="user-username">
                                            {{ user.username }}
                                        </div>
                                        <div class="user-genre-title-container">
                                            <div class="user-genre">
                                                {{
                                                    user.genre.replace("_", " ")
                                                }}
                                            </div>
                                            <div
                                                class="user-title"
                                                :class="{
                                                    'user-title-anonymous':
                                                        user.title ===
                                                        'ANONYMOUS',
                                                    'user-title-rising-star':
                                                        user.title ===
                                                        'RISING_STAR',
                                                    'user-title-legend':
                                                        user.title === 'LEGEND',
                                                }"
                                            >
                                                {{
                                                    user.title.replace("_", " ")
                                                }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="register-user content-node">
                            <div class="content-node-title-container">
                                Register new user
                            </div>
                            <div class="register-user-form">
                                <form class="form-wrapper">
                                    <div class="control-wrapper">
                                        <span class="input-label"
                                            >Username*
                                        </span>
                                        <input
                                            class="control transition-ease"
                                            v-model="newUser.username"
                                            type="text"
                                            placeholder="Enter your username"
                                        />
                                    </div>
                                    <div class="control-wrapper">
                                        <span class="input-label">Genre* </span>
                                        <select
                                            class="control transition-ease"
                                            v-model="newUser.genre"
                                        >
                                            <option
                                                v-for="(genre, idx) in genres"
                                                :key="idx"
                                                :value="genre"
                                            >
                                                {{ genre }}
                                            </option>
                                        </select>
                                    </div>
                                    <div class="submit-container">
                                        <input
                                            class="submit-button clickable primary-comp transition-ease-in"
                                            type="submit"
                                            @click.prevent="registerUser"
                                        />
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div class="release-song content-node">
                        <div class="content-node-title-container">
                            Release a song
                        </div>
                        <div class="register-user-form">
                            <form class="form-wrapper">
                                <div class="control-wrapper">
                                    <span class="input-label"
                                        >Artist* (selected by choosing a user)
                                    </span>
                                    <input
                                        class="control transition-ease"
                                        v-model="newSong.artist"
                                        type="text"
                                        disabled
                                    />
                                </div>
                                <div class="control-wrapper">
                                    <span class="input-label">Title* </span>
                                    <input
                                        class="control transition-ease"
                                        v-model="newSong.title"
                                        type="text"
                                        placeholder="Enter song title"
                                    />
                                </div>
                                <div class="control-wrapper">
                                    <span class="input-label"
                                        >Duration* (in seconds)
                                    </span>
                                    <input
                                        class="control transition-ease"
                                        v-model="newSong.duration"
                                        type="number"
                                        min="1"
                                        step="1"
                                        placeholder="Enter song duration"
                                    />
                                </div>
                                <div class="submit-container">
                                    <input
                                        class="submit-button clickable primary-comp transition-ease-in"
                                        type="submit"
                                        @click.prevent="releaseSong"
                                    />
                                </div>
                            </form>
                        </div>
                        <div class="logo-nav logo-promo">
                            <img
                                class="logo-img"
                                alt="logo"
                                src="@/assets/loudsound-icon.png"
                            />
                            <div class="app-name gradient-text">LoudSound</div>
                        </div>

                        <div class="promo gradient-text">
                            One song away from your dreams coming true
                        </div>
                    </div>
                </div>
                <div class="main-column main-column-right">
                    <div class="recommended-songs content-node">
                        <div class="content-node-title-container">
                            Recommended songs
                        </div>
                        <div class="songs-container">
                            <song-interactor
                                v-for="(songExpo, idx) in recommendedSongs"
                                :key="idx"
                                :songExpo="songExpo"
                                :isPlaying="songPlayingId === songExpo.song.id"
                            />
                        </div>
                    </div>
                    <div class="other-songs content-node">
                        <div class="content-node-title-container">
                            Also popular
                        </div>
                        <div class="songs-container">
                            <song-interactor
                                v-for="(songExpo, idx) in otherSongs"
                                :key="idx"
                                :songExpo="songExpo"
                                :isPlaying="songPlayingId === songExpo.song.id"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import swalToast from "@/mixins/swal-toast.js";
import userMixin from "@/mixins/users-mixin.js";
import songMixin from "@/mixins/song-mixin.js";
import SongInteractor from "@/components/SongInteractor.vue";

export default {
    components: {
        SongInteractor,
    },
    mixins: [swalToast, userMixin, songMixin],

    methods: {
        announce(message) {
            this.toast.fire({
                icon: "success",
                title: message,
            });
        },
        handleRequest(callback) {
            try {
                callback();
            } catch (error) {
                this.handle(error);
            }
        },
        handle(error) {
            this.toast.fire({
                icon: "warning",
                title: error.message,
            });
        },
    },
};
</script>

<style>
.top-nav {
    display: flex;
    align-items: center;
    padding: 5px 5%;
    background: var(--background);
    border-bottom: 2px var(--primary-dark) solid;
    height: 60px;
}

.logo-nav {
    display: flex;
    align-items: center;
}

.logo-img {
    width: 64px;
    height: auto;
    margin-right: 10px;
}

.app-name {
    font-size: 1.8rem;
}

.container {
    background-image: -webkit-linear-gradient(
        -60deg,
        var(--primary-dark) 0%,
        var(--primary-comp-dark) 25%,
        var(--primary-dark) 64%,
        var(--primary-comp-dark) 90%
    );
    background-image: -moz-linear-gradient(
        -60deg,
        var(--primary-dark) 0%,
        var(--primary-comp-dark) 25%,
        var(--primary-dark) 64%,
        var(--primary-comp-dark) 90%
    );
    background-image: -ms-linear-gradient(
        -60deg,
        var(--primary-dark) 0%,
        var(--primary-comp-dark) 25%,
        var(--primary-dark) 64%,
        var(--primary-comp-dark) 90%
    );
    background-image: -o-linear-gradient(
        -60deg,
        var(--primary-dark) 0%,
        var(--primary-comp-dark) 25%,
        var(--primary-dark) 64%,
        var(--primary-comp-dark) 90%
    );
    background-image: linear-gradient(
        -60deg,
        var(--primary-dark) 0%,
        var(--primary-comp-dark) 25%,
        var(--primary-dark) 64%,
        var(--primary-comp-dark) 90%
    );
}

.content {
    height: 95%;
    margin: 1% 5%;
    border-radius: 0.7em;
    display: flex;
}

.content-node {
    background: var(--background);
    border: 3px solid var(--background-lighter);
    border-radius: 0.4em;
}

.content-node-title-container {
    color: var(--control-border-color-focused);
    font-size: 1.5rem;
    margin-top: 10px;
    margin-bottom: 10px;
}

.main-column {
    margin: 5px;
    display: flex;
}

.main-column-left {
    flex: 55%;
}

.choose-and-register-user {
    flex: 45%;
    display: flex;
    flex-direction: column;
    margin-right: 10px;
}

.choose-user {
    flex: 45%;
    max-height: 45%;
    margin-bottom: 8px;
}

.register-user {
    flex: 55%;
}

.release-song {
    flex: 40%;
}

.main-column-right {
    flex: 40%;
    display: flex;
    flex-direction: column;
}

.recommended-songs {
    flex: 45%;
    max-height: 45%;
    margin-bottom: 8px;
}

.other-songs {
    flex: 55%;
    max-height: 52%;
}

.control {
    background: transparent;
    padding: 0.75em 1em !important;
    color: var(--control-border-color);
}

.control:focus {
    border-color: var(--primary-comp);
}

::-webkit-scrollbar {
    width: 10px;
}

/* Track */
::-webkit-scrollbar-track {
    background: var(--background-lighter);
    border-radius: 0.7em;
}

/* Handle */
::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 0.7em;
}

/* Handle on hover */
::-webkit-scrollbar-thumb:hover {
    background: #555;
}

.logo-promo {
    margin-top: 5em;
    justify-content: center;
}

.promo {
    font-size: 1.1rem;
    margin-top: 0.5em;
}

.songs-container {
    max-height: 84%;
    padding: 0 0.6em;
    overflow-y: auto;
    overflow-x: hidden;
    box-sizing: border-box;
}

@media screen and (min-device-width: 1200px) and (max-device-width: 1600px) and (-webkit-min-device-pixel-ratio: 1) {
    .top-nav {
        padding: 5px 2%;
    }

    .app-name {
        font-size: 1.65rem;
    }

    .logo-img {
        width: 58px;
        margin-right: 10px;
    }

    .content {
        height: 100%;
        margin: 0;
        border-radius: 0;
        border: 0;
    }
}
</style>
